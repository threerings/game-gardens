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

import java.io.IOException;
import java.util.logging.Level;

import com.samskivert.util.LoggingLogProvider;
import com.samskivert.util.OneLineLogFormatter;

import com.threerings.media.FrameManager;

import com.threerings.presents.client.Client;

import static com.threerings.toybox.Log.log;

/**
 * The launcher application for all ToyBox games.
 */
public class ToyBoxApp
{
    public void init (String username, String gameId)
        throws IOException
    {
        // create a frame
        _frame = new ToyBoxFrame("...", gameId, username);
        _framemgr = FrameManager.newInstance(_frame);

        // create and initialize our client instance
        _client = new ToyBoxClient();
        _client.init(_frame);
    }

    public void run (String server, int port, String username, String password)
    {
        // show the frame
        _frame.setVisible(true);

        Client client = _client.getContext().getClient();

        // pass them on to the client
        log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, port);

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(
                LogonPanel.createCredentials(username, password));
            client.logon();
        }

        _framemgr.start();
    }

    /**
     * Performs the standard setup and starts the ToyBox client application.
     */
    public static void start (ToyBoxApp app, String server, int port,
                              String username, String password)
    {
        // set up the proper logging services
        com.samskivert.util.Log.setLogProvider(new LoggingLogProvider());
        OneLineLogFormatter.configureDefaultHandler();

        try {
            // initialize the app
            app.init(username, System.getProperty("game_id"));
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Error initializing application.", ioe);
        }

        // and run it
        app.run(server, port, username, password);
    }

    public static void main (String[] args)
    {
        String server = "localhost";
        if (args.length > 0) {
            server = args[0];
        }

        int port = Client.DEFAULT_SERVER_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.err.println(
                    "Invalid port specification '" + args[1] + "'.");
            }
        }

        String username = (args.length > 2) ? args[2] : null;
        String password = (args.length > 3) ? args[3] : null;

        start(new ToyBoxApp(), server, port, username, password);
    }

    protected ToyBoxClient _client;
    protected ToyBoxFrame _frame;
    protected FrameManager _framemgr;
}
