//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005 Three Rings Design, Inc., All Rights Reserved
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

import java.io.IOException;
import java.util.logging.Level;

import com.samskivert.util.LoggingLogProvider;
import com.samskivert.util.OneLineLogFormatter;

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
    // from interface ToyBoxClient.Shell
    public void setTitle (String title)
    {
        // TODO
    }

    // from interface ToyBoxClient.Shell
    public void bindCloseAction (ToyBoxClient client)
    {
        // no need to do anything here
    }

    @Override // from Applet
    public void init ()
    {
        // set up the proper logging services
        com.samskivert.util.Log.setLogProvider(new LoggingLogProvider());
        OneLineLogFormatter.configureDefaultHandler();

        log.info("Java: " + System.getProperty("java.version") +
            ", " + System.getProperty("java.vendor") + ")");

        // create our frame manager
        _framemgr = FrameManager.newInstance(this);

        try {
            // create and initialize our client instance
            _client = new ToyBoxClient();
            _client.init(this);
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Failed to create ToyBoxClient.", ioe);
            return;
        }

        // configure our server and port
        String server = null;
        int port = 0;
        try {
            server = getParameter("server");
            port = Integer.parseInt(getParameter("port"));
        } catch (Exception e) {
            // fall through and complain
        }
        if (server == null || port <= 0) {
            log.warning("Failed to obtain server and port parameters " +
                "[server=" + server + ", port=" + port + "].");
            return;
        }
        Client client = _client.getContext().getClient();
        log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, new int[] { port });

        // and our game id
        String idstr = getParameter("game_id");
        try {
            _client.getContext().getToyBoxDirector().setGameId(
                Integer.parseInt(idstr));
        } catch (Exception e) {
            log.warning("Invalid game_id property supplied '" +
                idstr + "': " + e + ".");
        }
    }

    @Override // from Applet
    public void start ()
    {
        _framemgr.start();
    }

    @Override // from Applet
    public void stop ()
    {
        _framemgr.stop();

        // if we're logged on, log off
        if (_client != null) {
            Client client = _client.getContext().getClient();
            if (client != null && client.isLoggedOn()) {
                client.logoff(true);
            }
        }
    }

    protected ToyBoxClient _client;
    protected FrameManager _framemgr;
}
