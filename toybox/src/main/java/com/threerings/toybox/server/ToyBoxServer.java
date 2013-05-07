//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.server;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.depot.PersistenceContext;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.Config;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.DummyAuthenticator;
import com.threerings.presents.server.PresentsSession;
import com.threerings.presents.server.SessionFactory;

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
    public static class ToyBoxModule extends CrowdServer.CrowdModule {
        @Override protected void configure () {
            super.configure();
            bind(PlaceRegistry.class).to(ToyBoxPlaceRegistry.class);
            bind(ToyBoxConfig.class).toInstance(config());
            bind(Authenticator.class).to(autherClass());
            bind(ConnectionProvider.class).toInstance(conprov());
        }

        protected ToyBoxConfig config () {
            return new ToyBoxConfig(new Config(ToyBoxConfig.testConfig()));
        }
        protected ConnectionProvider conprov () {
            return StaticConnectionProvider.forTest("gardens");
        }
        protected Class<? extends Authenticator> autherClass () {
            return DummyAuthenticator.class;
        }
    }

    /**
     * Runs a ToyBox server in test configuration.
     */
    public static void main (String[] args)
    {
        runServer(new ToyBoxModule(), new PresentsServerModule(ToyBoxServer.class));
    }

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client manager to use the appropriate client class
        _clmgr.setDefaultSessionFactory(new SessionFactory() {
            @Override
            public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
                return ToyBoxSession.class;
            }
            @Override
            public Class<? extends ClientResolver> getClientResolverClass (Name username) {
                return ToyBoxClientResolver.class;
            }
        });

        // determine whether we've been run in test mode with a single game configuration
        String gconfig = System.getProperty("game_conf");
        PersistenceContext pctx = null;
        ToyBoxRepository toyrepo = null;
        if (StringUtil.isBlank(gconfig)) {
            pctx = new PersistenceContext();
            toyrepo = new ToyBoxRepository(pctx);
            pctx.init(ToyBoxRepository.GAME_DB_IDENT, _conprov, null);
            pctx.initializeRepositories(true);
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
    @Override
    protected int[] getListenPorts ()
    {
        return new int[] { _config.getServerPort() };
    }

    @Singleton
    protected static class ToyBoxPlaceRegistry extends PlaceRegistry {
        @Inject public ToyBoxPlaceRegistry (Lifecycle cycle) {
            super(cycle);
        }
        @Override protected PlaceManager createPlaceManager (PlaceConfig config) throws Exception {
            ClassLoader loader = _toymgr.getClassLoader(config);
            if (loader == null) {
                return super.createPlaceManager(config);
            }
            PlaceManager mgr = (PlaceManager)Class.forName(
                config.getManagerClassName(), true, loader).newInstance();
            _injector.injectMembers(mgr);
            return mgr;
        }
        @Inject protected ToyBoxManager _toymgr;
    }

    @Inject protected ToyBoxConfig _config;
    @Inject protected ParlorManager _parmgr;
    @Inject protected ToyBoxManager _toymgr;
    @Inject protected ConnectionProvider _conprov;
}
