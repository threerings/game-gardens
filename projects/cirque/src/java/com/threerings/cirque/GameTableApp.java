//
// $Id: GameTableApp.java,v 1.1 2004/01/20 14:35:13 mdb Exp $

package com.threerings.gametable;

import java.io.File;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

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

import com.threerings.toybox.server.ToyBoxManager;
import com.threerings.toybox.server.persist.ToyBoxRepository;

/**
 * Contains references to application-wide resources (like the database
 * repository) and handles initialization and cleanup for those resources.
 */
public class GameTableApp extends Application
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

    /** Provides access to the toybox manager. */
    public ToyBoxManager getToyBoxManager ()
    {
        return _tbmgr;
    }

    /** Provides access to the toybox repository. */
    public ToyBoxRepository getToyBoxRepository ()
    {
        return _tbmgr.getToyBoxRepository();
    }

    /**
     * Looks up a configuration property in our
     * <code>gametable.properties</code> application configuration file.
     */
    public String getProperty (String key)
    {
        return _config.getProperty(key);
    }

    /** Initialize the user management application. */
    protected void willInit (ServletConfig config)
    {
        super.willInit(config);

	try {
            // create a static connection provider
            _conprov = new StaticConnectionProvider(CONN_CONFIG);

            // load up our configuration properties
            _config = ConfigUtil.loadProperties(GAMETABLE_CONFIG);

            // create our repositories and managers
	    _usermgr = new UserManager(_config, _conprov);
            _tbmgr = new ToyBoxManager(_config, _conprov);

            // load up our build stamp so that we can report it
            String bstamp = PropertiesUtil.loadAndGet(
                "build.properties", "build.time");
	    Log.info("GameTable application initialized " +
                     "[built=" + bstamp + "].");

	} catch (Throwable t) {
	    Log.warning("Error initializing application: " + t);
	    Log.logStackTrace(t);
	}
    }

    /** Shut down the user management application. */
    public void shutdown ()
    {
	try {
	    _usermgr.shutdown();
	    Log.info("GameTable application shutdown.");

	} catch (Throwable t) {
	    Log.warning("Error shutting down repository: " + t);
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

    /** Manages our repository of game information. */
    protected ToyBoxManager _tbmgr;

    /** Our application configuration information. */
    protected Properties _config;

    /** The path to our database configuration file. */
    protected static final String CONN_CONFIG = "repository.properties";

    /** The path to our webapp configuration file. */
    protected static final String GAMETABLE_CONFIG = "gametable.properties";
}
