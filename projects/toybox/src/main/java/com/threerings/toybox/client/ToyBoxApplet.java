//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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

import java.io.IOException;
import java.net.URL;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJApplet;

import com.threerings.presents.client.Client;

import static com.threerings.toybox.Log.log;

/**
 * Launches a ToyBox game from an applet.
 */
public class ToyBoxApplet extends ManagedJApplet
    implements ToyBoxClient.Shell
{
    // from interface ToyBoxSession.Shell
    public void setTitle (String title)
    {
        // TODO
    }

    // from interface ToyBoxSession.Shell
    public void bindCloseAction (ToyBoxClient client)
    {
        // no need to do anything here
    }

    @Override // from Applet
    public void init ()
    {
        super.init();

        log.info("Java: " + System.getProperty("java.version") +
            ", " + System.getProperty("java.vendor") + ")");

        // create our frame manager
        _framemgr = FrameManager.newInstance(this);

        try {
            // create and initialize our client instance
            _client = createClient();
            _client.init(this);
        } catch (IOException ioe) {
            log.warning("Failed to create ToyBoxSession.", ioe);
            return;
        }

        // configure our server and port
        String server = getParameter("server");
        int port = getIntParameter("port", -1);
        if (server == null || port <= 0) {
            log.warning("Failed to obtain server and port parameters [server=" + server +
                        ", port=" + port + "].");
            return;
        }
        Client client = _client.getContext().getClient();
        log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, new int[] { port });

        // and our resource url
        ToyBoxDirector toydtr = _client.getContext().getToyBoxDirector();
        String resourceURL = getParameter("resource_url");
        try {
            toydtr.setResourceURL(new URL(resourceURL));
        } catch (Exception e) {
            log.warning("Invalid resource_url supplied '" +
                        resourceURL + "': " + e + ".");
        }

        // and our game id and game oid
        toydtr.setGameId(getIntParameter("game_id", -1), getIntParameter("game_oid", -1));
    }

    @Override // from Applet
    public void start ()
    {
        super.start();
        _framemgr.start();
    }

    @Override // from Applet
    public void stop ()
    {
        super.stop();
        _framemgr.stop();

        // if we're logged on, log off
        if (_client != null) {
            Client client = _client.getContext().getClient();
            if (client != null && client.isLoggedOn()) {
                client.logoff(true);
            }
        }
    }

    /** Helpy helper function. */
    protected int getIntParameter (String name, int defvalue)
    {
        try {
            return Integer.parseInt(getParameter(name));
        } catch (Exception e) {
            return defvalue;
        }
    }

    /**
     * Creates our client implementation.
     */
    protected ToyBoxClient createClient ()
    {
        return new ToyBoxClient();
    }

    protected ToyBoxClient _client;
    protected FrameManager _framemgr;
}
