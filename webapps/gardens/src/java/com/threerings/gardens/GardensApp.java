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

package com.threerings.gardens;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.samskivert.depot.PersistenceContext;
import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;

import com.samskivert.servlet.JDBCTableSiteIdentifier;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.servlet.user.UserManager;
import com.samskivert.velocity.Application;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.ServiceUnavailableException;

import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.ToyBoxRepository;

import static com.threerings.gardens.Log.log;

/**
 * Contains references to application-wide resources (like the database
 * repository) and handles initialization and cleanup for those resources.
 */
public class GardensApp extends Application
{
    /** Returns the connection provider in use by this application. */
    public ConnectionProvider getConnectionProvider ()
    {
        return _conprov;
    }

    /** Returns the user manager in use by this application. */
    public UserManager getUserManager ()
    {
        return _usermgr;
    }

    /** Provides access to the toybox repository. */
    public ToyBoxRepository getToyBoxRepository ()
    {
        return _tbrepo;
    }

    /**
     * Looks up a configuration property in our
     * <code>gardens.properties</code> application configuration file.
     */
    public String getProperty (String key)
    {
        return _config.getProperty(key);
    }

    // documentation inherited
    protected void configureVelocity (ServletConfig config, Properties props)
    {
        String ipath = config.getServletContext().getRealPath("/");
        if (ipath != null && ipath.indexOf("cache") == -1 && new File(ipath).exists()) {
            props.setProperty("file.resource.loader.path", ipath);
            log.info("Velocity loading directly from " + ipath + ".");
        }
    }

    /** Initialize the user management application. */
    protected void willInit (ServletConfig config)
    {
        super.willInit(config);

        try {
            // load up our configuration properties
            _config = ToyBoxConfig.config.getSubProperties("web");

            // create a static connection provider
            _conprov = new StaticConnectionProvider(
                ToyBoxConfig.getJDBCConfig());

            // create our repositories and managers
            String umclass = _config.getProperty(
                "webapp_auth", UserManager.class.getName());
            _usermgr = (UserManager)Class.forName(umclass).newInstance();
            _usermgr.init(_config, _conprov);

            PersistenceContext pctx = new PersistenceContext();
            pctx.init(ToyBoxRepository.GAME_DB_IDENT, _conprov, null);
            _tbrepo = new ToyBoxRepository(pctx);
            pctx.initializeRepositories(true);

            // load up our build stamp so that we can report it
            String bstamp = PropertiesUtil.loadAndGet("build.properties", "build.time");
            log.info("Game Gardens application initialized [built=" + bstamp + "].");

        } catch (Throwable t) {
            log.log(Level.WARNING, "Error initializing application", t);
        }
    }

    /** Shut down the user management application. */
    public void shutdown ()
    {
        try {
            _usermgr.shutdown();
            log.info("Game Gardens application shutdown.");

        } catch (Throwable t) {
            log.log(Level.WARNING, "Error shutting down repository", t);
        }
    }

    /** We want a special site identifier. */
    protected SiteIdentifier createSiteIdentifier (ServletContext ctx)
    {
        try {
            return new JDBCTableSiteIdentifier(_conprov);
        } catch (PersistenceException pe) {
            throw new ServiceUnavailableException(
                "Can't access site database.", pe);
        }
    }

    /** A reference to our user manager. */
    protected UserManager _usermgr;

    /** A reference to our connection provider. */
    protected ConnectionProvider _conprov;

    /** Our repository of game information. */
    protected ToyBoxRepository _tbrepo;

    /** Our application configuration information. */
    protected Properties _config;

    /** Used to configure velocity to load files right out of the
     * development directory. */
    protected static final String VEL_RELOAD_KEY = "web.velocity_file_loader";
}
