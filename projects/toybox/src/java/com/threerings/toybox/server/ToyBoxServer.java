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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.ClientFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsClient;
import com.threerings.presents.server.ShutdownManager;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.server.ParlorManager;

import com.threerings.toybox.server.persist.ToyBoxRepository;

import static com.threerings.toybox.Log.log;

/**
 * The main entry point and general organizer of everything that goes on
 * in the ToyBox game server process.
 */
public class ToyBoxServer extends CrowdServer
{
    /** Configures dependencies needed by the ToyBox services. */
    public static class Module extends CrowdServer.Module
    {
        @Override protected void configure () {
            super.configure();
            bind(PlaceRegistry.class).to(ToyBoxPlaceRegistry.class);
            bind(Authenticator.class).to(ToyBoxConfig.getAuthenticator());
            bind(ConnectionProvider.class).toInstance(
                new StaticConnectionProvider(ToyBoxConfig.getJDBCConfig()));
        }
    }

    /**
     * The main entry point for the ToyBox server.
     */
    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new Module());
        ToyBoxServer server = injector.getInstance(ToyBoxServer.class);
        try {
            server.init(injector);
            server.run();
        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
        }
    }

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client manager to use the appropriate client class
        _clmgr.setClientFactory(new ClientFactory() {
            public Class<? extends PresentsClient> getClientClass (AuthRequest areq) {
                return ToyBoxClient.class;
            }
            public Class<? extends ClientResolver> getClientResolverClass (Name username) {
                return ToyBoxClientResolver.class;
            }
        });

        // determine whether we've been run in test mode with a single game configuration
        String gconfig = System.getProperty("game_conf");
        ToyBoxRepository toyrepo = null;
        if (StringUtil.isBlank(gconfig)) {
            toyrepo = new ToyBoxRepository(_conprov);
        }
        _toymgr.init(toyrepo);
        if (!StringUtil.isBlank(gconfig)) {
            _toymgr.setDevelopmentMode(new File(gconfig));
        }

        log.info("ToyBox server initialized.");
    }

    /**
     * Returns the port on which the connection manager will listen for client connections.
     */
    protected int[] getListenPorts ()
    {
        return new int[] { ToyBoxConfig.getServerPort() };
    }

    @Singleton
    protected static class ToyBoxPlaceRegistry extends PlaceRegistry {
        @Inject public ToyBoxPlaceRegistry (ShutdownManager shutmgr) {
            super(shutmgr);
        }
        @Override protected PlaceManager createPlaceManager (PlaceConfig config) throws Exception {
            ClassLoader loader = _toymgr.getClassLoader(config);
            if (loader == null) {
                return super.createPlaceManager(config);
            }
            return (PlaceManager)Class.forName(
                config.getManagerClassName(), true, loader).newInstance();
        }
        @Inject protected ToyBoxManager _toymgr;
    }

    @Inject protected ParlorManager _parmgr;
    @Inject protected ToyBoxManager _toymgr;
    @Inject protected ConnectionProvider _conprov;
}
