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

package com.threerings.toybox.client;

import java.io.File;
import java.security.MessageDigest;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import com.samskivert.util.StringUtil;

import com.threerings.getdown.data.Resource;
import com.threerings.getdown.launcher.Downloader;

import com.threerings.resource.ResourceManager;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.client.GameReadyObserver;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.lobby.data.LobbyObject;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.Library;
import com.threerings.toybox.data.ToyBoxBootstrapData;
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
     * Our custom location director obtains a special class loader by
     * calling this method. We use this to run gamecode in a sandbox.
     */
    public ClassLoader getClassLoader (PlaceConfig config)
    {
        if (config instanceof ToyBoxGameConfig) {
            // return our already configured class loader as we're in
            // development mode and set it up when we entered the lobby
            return _gameLoader;
        }
        return null;
    }

    /**
     * Returns the resource manager that is usable by the game to load
     * custom resources from its jar file.
     */
    public ResourceManager getResourceManager ()
    {
        return _gameResource;
    }

    /**
     * This is called by a lobby controller when we have arrived safely
     * and soundly in our desired lobby.
     */
    public void enteredLobby (LobbyConfig config)
    {
        // configure our custom classloader (TODO: fire up a whole
        // separate connection to the game server and configure that with
        // the classloader when we're in "production" mode)
        _gameLoader = ToyBoxUtil.createClassLoader(
            _cacheDir, config.getGameDefinition());

        // create a resource manager that the game can use to load its
        // custom resources; this must be done here as the game code does
        // not have the necessary access privileges to run a resource
        // manager
        _gameResource = new ResourceManager("rsrc", _gameLoader);
        _ctx.getClient().setClassLoader(_gameLoader);

        // configure our message manager with this class loader so that we
        // can obtain translation resources from the game message bundles
        _ctx.getMessageManager().setClassLoader(_gameLoader);
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // obtain our root resource URL
        ToyBoxBootstrapData bstrap = (ToyBoxBootstrapData)
            client.getBootstrapData();
        try {
            _resourceURL = new URL(bstrap.resourceURL);
        } catch (Exception e) {
            log.log(Level.WARNING, "Invalid resource URL. We will be " +
                    "unable to load game code or media.", e);
        }

        // determine our local cache directory and make sure it exists
        _cacheDir = new File(ToyBoxClient.localDataDir("cache"));
        if (!_cacheDir.exists()) {
            File libdir = new File(_cacheDir, LIBRARY_DIR);
            if (!_cacheDir.mkdirs()) {
                log.warning("Unable to create game cache '" + _cacheDir + "'.");
            } else if (!libdir.mkdirs()) {
                log.warning("Unable to create library cache '" + libdir + "'.");
            }
        }

        // determine which lobby we are to enter...
        int gameId = 1;
        String idstr = System.getProperty("game_id");
        try {
            // if none is specified, we're in testing mode and we assume 1
            if (!StringUtil.blank(idstr)) {
                gameId = Integer.parseInt(idstr);
            }
        } catch (Exception e) {
            log.warning("Invalid game_id property supplied [value=" + idstr +
                        ", error=" + e + "].");
        }

        // ...and issue a request to do so
        ToyBoxService.ResultListener rl = new ToyBoxService.ResultListener() {
            public void requestProcessed (Object result) {
                enterLobby((Integer)result);
            }

            public void requestFailed (String cause) {
                // TODO: report this error graphically
                log.warning("Failed to get lobby oid: " + cause + ".");
            }
        };
        log.fine("Requesting lobby oid [game=" + gameId + "].");
        _toysvc.getLobbyOid(client, gameId, rl);
    }

    // documentation inherited
    public boolean receivedGameReady (int gameOid)
    {
        // TODO: fire up a connection to the game server and do all the
        // custom jockeying to make that work
        _ctx.getLocationDirector().moveTo(gameOid);
        return true;
    }

    /**
     * Ensures that the resources for the specified game definition are
     * resolved.
     */
    public void resolveResources (
        final GameDefinition gamedef, final Downloader.Observer obs)
    {
        // if our resource URL is a file: URL, we can ignore this whole
        // process as we're running in testing mode and needn't worry
        if (_resourceURL.getProtocol().equals("file")) {
            return;
        }

        // create a thread that will resolve the resources as we at least
        // have to some MD5 grindy grindy if not some downloading
        Thread t = new Thread() {
            public void run () {
                resolveResourcesAsync(gamedef, obs);
            }
        };
        t.start();
    }

    /** Helper method for entering a lobby and reporting any failure. */
    protected void enterLobby (int lobbyOid)
    {
        log.fine("Entering lobby [oid=" + lobbyOid + "].");

        // wire up a location observer that can detect if we fail to make
        // it into our requested lobby
        LocationAdapter obs = new LocationAdapter() {
            public void locationDidChange (PlaceObject place) {
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
            public void locationChangeFailed (int placeId, String reason) {
                // TODO: report this error graphically
                log.warning("Failed to enter lobby: " + reason + ".");
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
        };
        _ctx.getLocationDirector().addLocationObserver(obs);
        _ctx.getLocationDirector().moveTo(lobbyOid);
    }

    // documentation inherited
    protected void fetchServices (Client client)
    {
        super.fetchServices(client);
        _toysvc = (ToyBoxService)client.requireService(ToyBoxService.class);
    }

    /**
     * <em>BEWARE:</em> This method is run in a separate thread. Don't do
     * anything foolish.
     */
    protected void resolveResourcesAsync (
        GameDefinition gamedef, Downloader.Observer obs)
    {
        // determine whether the game's libraries, or its game jar file
        // need to be downloaded
        ArrayList<Resource> rsrcs = new ArrayList<Resource>();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            log.log(Level.WARNING, "JVM does not support MD5!?", e);
            // we're in a bad way
            return;
        }

        synchronized (_pending) {
            // TODO: put game jars in gameId subdirectories
            // check whether the files exist and match their checksums
            Resource rsrc = checkResource(
                gamedef.getJarName(), gamedef.getJarName(), md, gamedef.digest);
            if (rsrc != null) {
                rsrcs.add(rsrc);
            }
            for (int ii = 0; ii < gamedef.libs.length; ii++) {
                rsrc = checkResource(gamedef.libs[ii].getFilePath(),
                                     gamedef.libs[ii].getURLPath(),
                                     md, gamedef.libs[ii].digest);
                if (rsrc != null) {
                    rsrcs.add(rsrc);
                }
            }
        }

        // fire up a downloader to do the downloading, if there's nothing
        // to download it will just immediately call "downloadComplete()"
        // on the observer
        Downloader dloader = new Downloader(rsrcs, obs);
        // we're already on our own thread so just run() rather than start()
        dloader.run();
    }

    /** Helper function for {@link #resolveResourcesAsync}. */
    protected Resource checkResource (
        String lpath, String rpath, MessageDigest md, String rdigest)
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
            log.log(Level.WARNING, "Unable to construct URL for resource " +
                    "[local=" + lpath + ", remote=" + rpath + "].", e);
            return null;
        }

        // create a resource which the downloader will need
        Resource rsrc = new Resource(rpath, remote, local);

        // if the file already exists, check its MD5 hash
        if (rsrc.getLocal().exists()) {
            try {
                // TODO: display progress!
                String digest = rsrc.computeDigest(md, null);
                if (StringUtil.blank(rdigest) ||
                    digest.equals(rdigest)) {
                    log.info("Resource up to date " + rsrc +
                             " (digest " + digest + ").");
                    return null;
                }

            } catch (Exception e) {
                log.info("Failed to compute digest, refetching " +
                         "[rsrc=" + rsrc + ", error=" + e + "].");
            }
        }

        // add it to the pending set and return it to the caller
        _pending.add(local);
        return rsrc;
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxService _toysvc;

    protected URL _resourceURL;
    protected File _cacheDir;

    protected ClassLoader _gameLoader;
    protected ResourceManager _gameResource;

    /** Contains an entry for all resources in the process of being
     * downloaded. */
    protected HashSet<File> _pending = new HashSet<File>();
}
