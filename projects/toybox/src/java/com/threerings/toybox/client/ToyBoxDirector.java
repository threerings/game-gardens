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
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import com.samskivert.util.StringUtil;

import com.threerings.getdown.data.Resource;
import com.threerings.getdown.launcher.Downloader;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.client.GameReadyObserver;

import com.threerings.toybox.lobby.data.LobbyObject;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.Library;
import com.threerings.toybox.data.ToyBoxBootstrapData;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

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
            ToyBoxGameConfig gconfig = (ToyBoxGameConfig)config;
            GameDefinition gamedef = gconfig.getGameDefinition();

            ArrayList<URL> ulist = new ArrayList<URL>();
            String path = "";
            try {
                // add the game jar file
                path = "file://" + _cacheDir + "/" + gamedef.getJarName();
                ulist.add(new URL(path));
                // enumerate the paths to the game's jar files
                for (int ii = 0; ii < gamedef.libs.length; ii++) {
                    Library lib = gamedef.libs[ii];
                    path = "file://" + _cacheDir + "/" + lib.getURLPath();
                    ulist.add(new URL(path));
                }

            } catch (Exception e) {
                log.warning("Failed to create URL for class loader " +
                            "[cache=" + _cacheDir + ", path=" + path +
                            ", error=" + e + "].");
            }

            return new URLClassLoader(ulist.toArray(new URL[ulist.size()]));
        }
        return null;
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
        final String ident = System.getProperty("game_ident");
        if (StringUtil.blank(ident)) {
            log.warning("Missing 'game_ident' system property.");
            return;
        }

        // ...and issue a request to do so
        ToyBoxService.ResultListener rl = new ToyBoxService.ResultListener() {
            public void requestProcessed (Object result) {
                enterLobby(ident, (Integer)result);
            }

            public void requestFailed (String cause) {
                // TODO: report this error graphically
                log.warning("Failed to get lobby oid [game=" + ident +
                            ", error=" + cause + "].");
            }
        };
        log.fine("Requesting lobby oid [ident=" + ident + "].");
        _toysvc.getLobbyOid(client, ident, rl);
    }

    // documentation inherited
    public boolean receivedGameReady (int gameOid)
    {
        return true;
    }

    /**
     * Ensures that the resources for the specified game definition are
     * resolved.
     */
    public void resolveResources (
        final GameDefinition gamedef, final Downloader.Observer obs)
    {
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
    protected void enterLobby (final String ident, int lobbyOid)
    {
        log.fine("Entering lobby [ident=" + ident + ", oid=" + lobbyOid + "].");

        // wire up a location observer that can detect if we fail to make
        // it into our requested lobby
        LocationAdapter obs = new LocationAdapter() {
            public void locationDidChange (PlaceObject place) {
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
            public void locationChangeFailed (int placeId, String reason) {
                // TODO: report this error graphically
                log.warning("Failed to enter lobby [game=" + ident +
                            ", error=" + reason + "].");
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

        // if the file already exists, check it's MD5 hash
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

    /** Contains an entry for all resources in the process of being
     * downloaded. */
    protected HashSet<File> _pending = new HashSet<File>();
}