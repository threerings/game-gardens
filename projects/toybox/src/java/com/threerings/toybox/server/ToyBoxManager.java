//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.gamegardens.com/code/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.toybox.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.security.MessageDigest;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.util.Invoker;
import com.samskivert.util.ResultListenerList;

import org.apache.commons.io.IOUtils;

import com.threerings.getdown.data.Resource;

import com.threerings.presents.client.InvocationService.ResultListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.util.ResultAdapter;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.lobby.server.LobbyManager;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.server.persist.Game.Status;
import com.threerings.toybox.server.persist.Game;
import com.threerings.toybox.server.persist.ToyBoxRepository;
import com.threerings.toybox.util.ToyBoxClassLoader;
import com.threerings.toybox.util.ToyBoxUtil;

import static com.threerings.toybox.Log.log;
import static com.threerings.toybox.data.ToyBoxCodes.*;

/**
 * Manages the server side of the ToyBox services.
 */
public class ToyBoxManager
    implements ToyBoxProvider
{
    /**
     * Prepares the toybox manager for operation in a server managing a
     * collection of games in conjunction with a repository.
     */
    public void init (InvocationManager invmgr, ConnectionProvider conprov)
        throws PersistenceException
    {
        // create our repository
        _toyrepo = new ToyBoxRepository(conprov);

        // perform common initializations
        finishInit(invmgr);
    }

    /**
     * Prepares the toybox manager for operation in development mode where
     * it only hosts the lobby for a single game, which we will create
     * immediately rather than on-demand.
     */
    public void init (InvocationManager invmgr, File gameConfig)
        throws PersistenceException
    {
        // perform common initializations
        finishInit(invmgr);

        // create a fake game record for this game and resolve its lobby
        Game game = new Game();
        GameDefinition gamedef = null;
        try {
            game.gameId = 1;
            game.name = "test";
            game.maintainerId = 1;
            game.setStatus(Status.READY);
            game.definition = IOUtils.toString(new FileReader(gameConfig));

            // compute the digests of all the files
            gamedef = game.parseGameDefinition();
            File jar = new File(ToyBoxConfig.getResourceDir(),
                                gamedef.getJarName());
            log.info("Reading " + jar + "...");
            MessageDigest md = MessageDigest.getInstance("MD5");
            game.digest = Resource.computeDigest(jar, md, null);

        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load game config " +
                    "[path=" + gameConfig + "].", e);
            return;
        }

        try {
            resolveLobby(game);
        } catch (InvocationException ie) {
            log.log(Level.WARNING, "Failed to resolve lobby " +
                    "[game=" + game + "].", ie);
        }
    }

    /**
     * Handles initialization common to our two modes of operation.
     */
    protected void finishInit (InvocationManager invmgr)
    {
        // register ourselves as providing the toybox service
        invmgr.registerDispatcher(new ToyBoxDispatcher(this), true);

        log.info("ToyBoxManager ready [rsrcdir=" +
                 ToyBoxConfig.getResourceDir() + "].");
    }

    /**
     * Returns a reference to our repository.
     */
    public ToyBoxRepository getToyBoxRepository ()
    {
        return _toyrepo;
    }

    /**
     * Returns the custom class loader that should be used for the
     * specified place.
     */
    public ClassLoader getClassLoader (PlaceConfig config)
    {
        if (config instanceof ToyBoxGameConfig) {
            ToyBoxGameConfig tconfig = (ToyBoxGameConfig)config;
            String ident = tconfig.getManagerClassName();
            ToyBoxClassLoader loader = _loaders.get(ident);
            // create a classloader if we haven't yet, or if our
            // underlying jar files have changed since we created one
            if (loader == null || !loader.isUpToDate()) {
                loader = ToyBoxUtil.createClassLoader(
                    ToyBoxConfig.getResourceDir(),
                    tconfig.getGameDefinition());
                _loaders.put(ident, loader);
            }
            return loader;

        } else {
            return null;
        }
    }

    /**
     * Called to report the total playtime in a particular game. This
     * records the playtime persistently.
     *
     * @param game the game whose time is being reported.
     * @param playtime the total playtime in milliseconds.
     */
    public void recordPlaytime (final Game game, long playtime)
    {
        // we don't record playtime if we're in development mode
        if (_toyrepo == null) {
            return;
        }

        int mins = (int)(playtime / ONE_MINUTE);
        if (mins > ODDLY_LONG) {
            log.warning("Game in play for oddly long time " +
                        "[game=" + game.name + ", mins=" + mins + "].");
        }
        mins = Math.min(mins, MAX_PLAYTIME);
        if (mins <= 0) {
            return;
        }

        log.info("Recording playtime [game=" + game.name +
                 ", mins=" + mins + "].");

        final int fmins = mins;
        ToyBoxServer.invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                try {
                    _toyrepo.incrementPlaytime(game.gameId, fmins);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to update playtime " +
                            "[game=" + game.name + ", mins=" + fmins + "].", e);
                }
                return false;
            }
        });
    }

    // documentation inherited from interface
    public void getLobbyOid (ClientObject caller, final int gameId,
                             final ResultListener rl)
        throws InvocationException
    {
        // look to see if we have already resolved a lobby for this game
        Integer lobbyOid = _lobbyOids.get(gameId);
        if (lobbyOid != null) {
            rl.requestProcessed(lobbyOid);
            return;
        }

        // if we are currently loading this lobby, add this listener to
        // the list of penders
        ResultListenerList penders = _penders.get(gameId);
        if (penders != null) {
            penders.add(new ResultAdapter(rl));
            return;
        }

        // load the game information from the database
        ToyBoxServer.invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                try {
                    _game = _toyrepo.loadGame(gameId);
                } catch (PersistenceException pe) {
                    log.log(Level.WARNING, "Failed to load game " +
                            "[game=" + gameId + "].", pe);
                }
                return true;
            }

            public void handleResult () {
                // if we failed to load the game, stop now
                if (_game == null) {
                    rl.requestFailed(INTERNAL_ERROR);
                    return;
                }

                try {
                    // start the lobby resolution. if this fails we will
                    // catch the failure and report it to the caller
                    resolveLobby(_game);

                    // otherwise we're safe to finally map our result
                    // listener into a listener list
                    ResultListenerList rls = new ResultListenerList();
                    rls.add(new ResultAdapter(rl));
                    _penders.put(gameId, rls);

                } catch (InvocationException ie) {
                    rl.requestFailed(ie.getMessage());
                }
            }

            protected Game _game;
        });
    }

    /**
     * Resolves a lobby for the specified game definition. When the lobby
     * is fully resolved, all pending listeners will be notified of its
     * creation. See {@link #_penders}.
     *
     * @param game the metadata for the game whose lobby we will create.
     */
    public void resolveLobby (final Game game)
        throws InvocationException
    {
        log.info("Resolving " + game.which() + ".");

        PlaceRegistry.CreationObserver obs =
            new PlaceRegistry.CreationObserver() {
            public void placeCreated (PlaceObject place, PlaceManager pmgr) {
                // let our lobby manager know about its game
                ((LobbyManager)pmgr).setGame(game);
                // register ourselves in the lobby table
                _lobbyOids.put(game.gameId, place.getOid());
                // inform any resolution penders of the lobby oid
                ResultListenerList listeners = _penders.remove(game.gameId);
                if (listeners != null) {
                    listeners.requestCompleted(place.getOid());
                }
            }
        };
        try {
            ToyBoxServer.plreg.createPlace(
                new LobbyConfig(game.parseGameDefinition()), obs);
        } catch (InstantiationException e) {
            log.log(Level.WARNING, "Failed to create game lobby " +
                    "[game=" + game.which() + "]", e);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /**
     * Called by the {@link LobbyManager} when it shuts down.
     */
    public void lobbyDidShutdown (Game game)
    {
        if (_lobbyOids.remove(game.gameId) == null) {
            log.warning("Lobby shut down for which we have no registration " +
                        "[game=" + game.which() + "].");
        } else {
            log.info("Unloading lobby '" + game.which() + "'.");
        }
    }

    /** Our persistent repository. */
    protected ToyBoxRepository _toyrepo;

    /** Contains pending listeners for lobbies in the process of being
     * resolved. */
    protected HashMap<Integer,ResultListenerList> _penders =
        new HashMap<Integer,ResultListenerList>();

    /** Contains a mapping from game identifier strings to lobby oids for
     * lobbies that have been resolved. */
    protected HashMap<Integer,Integer> _lobbyOids =
        new HashMap<Integer,Integer>();

    /** Maps game identifiers to custom class loaders. In general this
     * will only have one mapping, but we'll be general just in case.  */
    protected HashMap<String,ToyBoxClassLoader> _loaders =
        new HashMap<String,ToyBoxClassLoader>();

    /** One minute in milliseconds. */
    protected static final long ONE_MINUTE = 60 * 1000L;

    /** The maximum playtime we will record for a game, in minutes. (This
     * is to avoid booching the stats if something goes awry.) */
    protected static final int MAX_PLAYTIME = 60;

    /** If a game is in play longer than this many minutes, we log a
     * warning when recording its playtime to catch funny business. */
    protected static final int ODDLY_LONG = 120;
}
