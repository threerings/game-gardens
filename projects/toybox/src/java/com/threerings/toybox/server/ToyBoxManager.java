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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.ResultListenerList;
import com.samskivert.jdbc.ConnectionProvider;

import org.apache.commons.io.IOUtils;

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
        try {
            game.gameId = 1;
            game.maintainerId = 1;
            game.setStatus(Status.PUBLISHED);
            game.definition = IOUtils.toString(new FileReader(gameConfig));
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Failed to load game config " +
                    "[path=" + gameConfig + "].", ioe);
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
            String name = tconfig.getGameName();
            ClassLoader loader = (ClassLoader)_loaders.get(name);
            if (loader == null) {
                loader = ToyBoxUtil.createClassLoader(
                    ToyBoxConfig.getResourceDir(),
                    tconfig.getGameDefinition());
                _loaders.put(name, loader);
            }
            return loader;

        } else {
            return null;
        }
    }

    // documentation inherited from interface
    public void getLobbyOid (ClientObject caller, String gameIdent,
                             ResultListener rl)
        throws InvocationException
    {
        // look to see if we have already resolved a lobby for this game
        Integer lobbyOid = _lobbyOids.get(gameIdent);
        if (lobbyOid != null) {
            rl.requestProcessed(lobbyOid);
            return;
        }

        // if we are currently loading this lobby, add this listener to
        // the list of penders
        ResultListenerList penders = _penders.get(gameIdent);
        if (penders != null) {
            penders.add(new ResultAdapter(rl));
            return;
        }

        // otherwise we will need to load the game information from the
        // repository and resolve a lobby for that game

        // TODO: look up the game from the repository, then:
        // - add the ResultListener to _penders
        // - call resolveLobby(game);
        throw new InvocationException(INTERNAL_ERROR);
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
        final GameDefinition gdef = game.parseGameDefinition();
        log.info("Resolving " + gdef + ".");

        PlaceRegistry.CreationObserver obs =
            new PlaceRegistry.CreationObserver() {
            public void placeCreated (PlaceObject place, PlaceManager pmgr) {
                // let the lobby manager know about the game record
                ((LobbyManager)pmgr).setGame(game);
                // register ourselves in the lobby table
                _lobbyOids.put(gdef.ident, place.getOid());
                // inform any resolution penders of the lobby oid
                ResultListenerList listeners = _penders.remove(game.ident);
                if (listeners != null) {
                    listeners.requestCompleted(place.getOid());
                }
            }
        };
        try {
            ToyBoxServer.plreg.createPlace(new LobbyConfig(gdef), obs);
        } catch (InstantiationException e) {
            log.log(Level.WARNING, "Failed to create game lobby " +
                    "[game=" + game.gameId + "]", e);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /** Our persistent repository. */
    protected ToyBoxRepository _toyrepo;

    /** Contains pending listeners for lobbies in the process of being
     * resolved. */
    protected HashMap<String,ResultListenerList> _penders =
        new HashMap<String,ResultListenerList>();

    /** Contains a mapping from game identifier strings to lobby oids for
     * lobbies that have been resolved. */
    protected HashMap<String,Integer> _lobbyOids =
        new HashMap<String,Integer>();

    /** Maps game identifiers to custom class loaders. In general this
     * will only have one mapping, but we'll be general just in case.  */
    protected HashMap<String,ClassLoader> _loaders =
        new HashMap<String,ClassLoader>();
}
