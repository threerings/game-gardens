//
// $Id: ToyBoxServer.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.server;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;

import com.threerings.crowd.server.CrowdClient;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.presents.client.Client;

import com.threerings.parlor.server.ParlorManager;

import com.threerings.toybox.Log;

/**
 * The main entry point and general organizer of everything that goes on
 * in the ToyBox game server process.
 */
public class ToyBoxServer extends CrowdServer
{
    /** The connection provider used to obtain access to our JDBC
     * databases. */
    public static ConnectionProvider conprov;

    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

    /** Handles ToyBox-specific functionality. */
    public static ToyBoxManager toymgr = new ToyBoxManager();

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the base server initialization
        super.init();

        // configure the client manager to use the appropriate client class
        clmgr.setClientClass(CrowdClient.class);

        // create our database connection provider
        conprov = new StaticConnectionProvider(ToyBoxConfig.getJDBCConfig());

        // initialize our managers
        parmgr.init(invmgr, plreg);
        toymgr.init(invmgr, conprov);

        Log.info("ToyBox server initialized.");
    }

    /**
     * Returns the port on which the connection manager will listen for
     * client connections.
     */
    protected int getListenPort ()
    {
        int port = Client.DEFAULT_SERVER_PORT;
        try {
            port = Integer.parseInt(System.getProperty("port"));
        } catch (Exception e) {
        }
        return port;
    }

    public static void main (String[] args)
    {
        ToyBoxServer server = new ToyBoxServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
        }
    }
}
