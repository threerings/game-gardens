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
import java.util.logging.Level;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.LoggingLogProvider;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.CrowdClient;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.server.ParlorManager;

import static com.threerings.toybox.Log.log;

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
        clmgr.setClientClass(ToyBoxClient.class);

        // create our database connection provider
        conprov = new StaticConnectionProvider(ToyBoxConfig.getJDBCConfig());

        // initialize our managers
        parmgr.init(invmgr, plreg);

        // determine whether we've been run in test mode with a single
        // game configuration
        String gconfig = System.getProperty("game_conf");
        if (StringUtil.blank(gconfig)) {
            toymgr.init(invmgr, conprov);
        } else {
            toymgr.init(invmgr, new File(gconfig));
        }

        log.info("ToyBox server initialized.");
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

    // documentation inherited
    protected PlaceRegistry createPlaceRegistry (
        InvocationManager invmgr, RootDObjectManager omgr)
    {
        return new PlaceRegistry(invmgr, omgr) {
            protected ClassLoader getClassLoader (PlaceConfig config) {
                ClassLoader loader = toymgr.getClassLoader(config);
                return (loader == null) ? super.getClassLoader(config) : loader;
            }
        };
    }

    public static void main (String[] args)
    {
        // set up the proper logging services
        com.samskivert.util.Log.setLogProvider(new LoggingLogProvider());

        ToyBoxServer server = new ToyBoxServer();
        try {
            server.init();
            server.run();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to initialize server.", e);
        }
    }
}
