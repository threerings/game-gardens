//
// $Id: ToyBoxApp.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.client;

import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.micasa.client.MiCasaClient;
import com.threerings.micasa.client.MiCasaFrame;

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
        _frame = new MiCasaFrame();

        // create our client instance
        String cclass = null;
        try {
            cclass = System.getProperty("client");
        } catch (Throwable t) {
            // security manager in effect, no problem
        }
        if (cclass == null) {
            cclass = MiCasaClient.class.getName();
        }

        try {
            _client = (MiCasaClient)Class.forName(cclass).newInstance();
        } catch (Exception e) {
            Log.warning("Unable to instantiate client class " +
                        "[cclass=" + cclass + "].");
            Log.logStackTrace(e);
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
            Log.warning("Error initializing application.");
            Log.logStackTrace(ioe);
        }

        // and run it
        app.run(server, port, username, password);
    }

    protected MiCasaClient _client;
    protected MiCasaFrame _frame;
}
