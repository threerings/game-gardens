//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
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

package com.threerings.toybox.client;

import java.io.File;
import java.security.MessageDigest;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.samskivert.util.StringUtil;

import com.threerings.getdown.data.Resource;
import com.threerings.getdown.net.HTTPDownloader;

import com.threerings.resource.ResourceManager;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.client.GameReadyObserver;

import com.threerings.toybox.lobby.data.LobbyConfig;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.toybox.util.ToyBoxUtil;

import static com.threerings.toybox.Log.log;
import static com.threerings.toybox.data.ToyBoxCodes.*;

/**
 * Handles the client side of the ToyBox services.
 */
public class ToyBoxDirector extends BasicDirector
    implements GameReadyObserver
{
    public ToyBoxDirector (ToyBoxContext ctx)
    {
        super(ctx);
        _ctx = ctx;

        // register to handle game entry with the parlor director
        _ctx.getParlorDirector().addGameReadyObserver(this);
    }

    /**
     * Our custom location director obtains a special class loader by calling this method. We use
     * this to run gamecode in a sandbox.
     */
    public ClassLoader getClassLoader (PlaceConfig config)
    {
        if (config instanceof ToyBoxGameConfig) {
            // return our already configured class loader as we're in development mode and set it
            // up when we entered the lobby
            return _gameLoader;
        }
        return null;
    }

    /**
     * Returns the resource manager that is usable by the game to load custom resources from its
     * jar file.
     */
    public ResourceManager getResourceManager ()
    {
        return _gameResource;
    }

    /**
     * This is called by a lobby controller when we have arrived safely and soundly in our desired
     * lobby.
     */
    public void enteredLobby (LobbyConfig config)
    {
        // configure our custom classloader (TODO: fire up a whole separate connection to the game
        // server and configure that with the classloader when we're in "production" mode)
        int gameId = config.getGameId();
        GameDefinition gamedef = config.getGameDefinition();
        String ident = gamedef.ident + "-" + gameId;
        _gameLoader = _cache.get(ident);
        if (_gameLoader == null) {
            _gameLoader = ToyBoxUtil.createClassLoader(_cacheDir, gameId, gamedef);
            _cache.put(ident, _gameLoader);
        }
        log.info("Configured game class loader", "game", ident, "loader", _gameLoader);

        // configure the resource manager to load files from the game's class loader
        _gameResource.setClassLoader(_gameLoader);
        _ctx.getClient().setClassLoader(_gameLoader);

        // configure our message manager with this class loader so that we can obtain translation
        // resources from the game message bundles
        _ctx.getMessageManager().setClassLoader(_gameLoader);
    }

    /**
     * Configures the id of the game we're playing.
     */
    public void setGameId (int gameId, int gameOid)
    {
        _gameId = gameId;
        _gameOid = gameOid;
    }

    /**
     * Configures the URL from which we download our game and library resources.
     */
    public void setResourceURL (URL resourceURL)
    {
        _resourceURL = resourceURL;
    }

    // documentation inherited
    @Override
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // determine our local cache directory and make sure it exists
        _cacheDir = new File(ToyBoxClient.localDataDir("cache"));
        if (!_cacheDir.exists()) {
            if (!_cacheDir.mkdirs()) {
                log.warning("Unable to create game cache '" + _cacheDir + "'.");
            }
        }

        // if we already have a game oid, then go directly there, otherwise first enter the lobby
        if (_gameOid > 0) {
            // TODO: we need to download the game code etc.
            _ctx.getLocationDirector().moveTo(_gameOid);
            return;
        }

        // issue a request to enter our game lobby
        ToyBoxService.ResultListener rl = new ToyBoxService.ResultListener() {
            public void requestProcessed (Object result) {
                enterLobby((Integer)result);
            }
            public void requestFailed (String cause) {
                // TODO: report this error graphically
                log.warning("Failed to get lobby oid [gameId=" + _gameId +
                            ", error=" + cause + "].");
            }
        };
        log.debug("Requesting lobby oid [game=" + _gameId + "].");
        _toysvc.getLobbyOid(client, _gameId, rl);
    }

    // documentation inherited
    public boolean receivedGameReady (int gameOid)
    {
        // TODO: fire up a connection to the game server and do all the custom jockeying to make
        // that work
        _ctx.getLocationDirector().moveTo(gameOid);
        return true;
    }

    /**
     * Ensures that the resources for the specified game definition are resolved.
     */
    public void resolveResources (final int gameId, final GameDefinition gamedef,
                                  final HTTPDownloader.Observer obs)
    {
        log.info("Resolving resources [game=" + gameId + ", rurl=" + _resourceURL + "].");

        // if our resource URL is a file: URL, we can ignore this whole process as we're running in
        // testing mode and needn't worry
        if (_resourceURL.getProtocol().equals("file")) {
            obs.downloadProgress(100, 0L);
            return;
        }

        // create a thread that will resolve the resources as we at least have to some MD5 grindy
        // grindy if not some downloading
        Thread t = new Thread() {
            @Override
            public void run () {
                resolveResourcesAsync(gameId, gamedef, obs);
            }
        };
        t.start();
    }

    /** Helper method for entering a lobby and reporting any failure. */
    protected void enterLobby (int lobbyOid)
    {
        log.debug("Entering lobby [oid=" + lobbyOid + "].");

        // wire up a location observer that can detect if we fail to make it into our lobby
        LocationAdapter obs = new LocationAdapter() {
            @Override
            public void locationDidChange (PlaceObject place) {
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
            @Override
            public void locationChangeFailed (int placeId, String reason) {
                // TODO: report this error graphically
                log.warning("Failed to enter lobby: " + reason + ".");
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
        };
        _ctx.getLocationDirector().addLocationObserver(obs);
        _ctx.getLocationDirector().moveTo(lobbyOid);
    }

    @Override // from BasicDirector
    protected void registerServices (Client client)
    {
        client.addServiceGroup(TOYBOX_GROUP);
    }

    @Override // from BasicDirector
    protected void fetchServices (Client client)
    {
        super.fetchServices(client);
        _toysvc = client.requireService(ToyBoxService.class);
    }

    /**
     * <em>BEWARE:</em> This method is run in a separate thread. Don't do anything foolish.
     */
    protected void resolveResourcesAsync (
        int gameId, GameDefinition gamedef, HTTPDownloader.Observer obs)
    {
        // determine whether the game's libraries, or its game jar file need to be downloaded
        ArrayList<Resource> rsrcs = new ArrayList<Resource>();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            log.warning("JVM does not support MD5!?", e);
            // we're in a bad way
            return;
        }

        synchronized (_pending) {
            // check whether the files exist and match their checksums
            Resource rsrc = checkResource(
                gamedef.getMediaPath(gameId), gamedef.getMediaPath(gameId), md, gamedef.digest);
            if (rsrc != null) {
                rsrcs.add(rsrc);
            }
        }

        // fire up a downloader to do the downloading, if there's nothing to download it will just
        // immediately call "downloadComplete()" on the observer
        HTTPDownloader dloader = new HTTPDownloader(rsrcs, obs);
        // we're already on our own thread so just run() rather than start()
        dloader.download();
    }

    /** Helper function for {@link #resolveResourcesAsync}. */
    protected Resource checkResource (String lpath, String rpath, MessageDigest md, String rdigest)
    {
        // if we're already downloading it, skip it
        File local = new File(_cacheDir, lpath);
        if (_pending.contains(local)) {
            return null;
        }

        // determine the library's remote URL
        URL remote;
        try {
            remote = new URL(_resourceURL, rpath);
        } catch (Exception e) {
            log.warning("Unable to construct URL for resource [local=" + lpath +
                    ", remote=" + rpath + "].", e);
            return null;
        }

        // create a resource which the downloader will need
        Resource rsrc = new Resource(rpath, remote, local, false);

        // if the file already exists, check its MD5 hash
        if (rsrc.getLocal().exists()) {
            try {
                // TODO: display progress!
                String digest = rsrc.computeDigest(md, null);
                if (StringUtil.isBlank(rdigest) || digest.equals(rdigest)) {
                    log.info("Resource up to date " + rsrc + " (digest " + digest + ").");
                    return null;
                }

            } catch (Exception e) {
                log.info("Failed to compute digest, refetching [rsrc=" + rsrc +
                         ", error=" + e + "].");
            }
        }

        // add it to the pending set and return it to the caller
        _pending.add(local);
        return rsrc;
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxService _toysvc;

    protected int _gameId = -1, _gameOid = -1;
    protected URL _resourceURL;
    protected File _cacheDir;

    protected ClassLoader _gameLoader;
    protected ResourceManager _gameResource = new ResourceManager("rsrc");

    /** Contains an entry for all resources in the process of being downloaded. */
    protected HashSet<File> _pending = new HashSet<File>();

    /** We have to cache our classloaders as we must preserve the same classloader for the lifetime
     * of the session so that the class cache held by the ObjectInputStream remains valid. */
    protected HashMap<String,ClassLoader> _cache = new HashMap<String,ClassLoader>();
}
