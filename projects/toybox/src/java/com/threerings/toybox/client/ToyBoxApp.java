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

import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.util.Name;

import com.threerings.toybox.Log;

/**
 * The launcher application for all ToyBox games.
 */
public class ToyBoxApp
{
    public void init ()
        throws IOException
    {
        // create a frame
        _frame = new ToyBoxFrame();

        // create our client instance
        String cclass = null;
        try {
            cclass = System.getProperty("client");
        } catch (Throwable t) {
            // security manager in effect, no problem
        }
        if (cclass == null) {
            cclass = ToyBoxClient.class.getName();
        }

        try {
            _client = (ToyBoxClient)Class.forName(cclass).newInstance();
        } catch (Exception e) {
            Log.warning("Unable to instantiate client class " +
                        "[cclass=" + cclass + "].", e);
        }

        // initialize our client instance
        _client.init(_frame);
    }

    public void run (String server, int port, String username, String password)
    {
        // position everything and show the frame
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);
        _frame.setVisible(true);

        Client client = _client.getContext().getClient();

        // pass them on to the client
        Log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, port);

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(
                new UsernamePasswordCreds(new Name(username), password));
            client.logon();
        }
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
                Log.warning("Invalid port specification '" + args[1] + "'.");
            }
        }

        String username = (args.length > 2) ? args[2] : null;
        String password = (args.length > 3) ? args[3] : null;

        ToyBoxApp app = new ToyBoxApp();
        try {
            // initialize the app
            app.init();
        } catch (IOException ioe) {
            Log.warning("Error initializing application.", ioe);
        }

        // and run it
        app.run(server, port, username, password);
    }

    protected ToyBoxClient _client;
    protected ToyBoxFrame _frame;
}
