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
import java.util.Properties;

import com.google.inject.Singleton;

import com.samskivert.util.Config;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.Client;

import static com.threerings.toybox.Log.log;

/**
 * Provides access to installation specific configuration parameters.
 */
@Singleton public class ToyBoxConfig
{
    /** Creates a properties instance with test configurations. */
    public static Properties testConfig () {
        String[] defprops = {
            "server_host", "localhost",
            "resource_dir", "server/target/games",
            "resource_url", "http://localhost:8080/games/",
        };
        Properties props = new Properties();
        for (int ii = 0; ii < defprops.length; ii += 2) {
            props.setProperty(defprops[ii], defprops[ii+1]);
        }
        return props;
    }

    public ToyBoxConfig (Config config) {
        _config = config;
    }

    /** Returns the main lobby server host. */
    public String getServerHost () {
        return requireValue("server_host");
    }

    /** Returns the port on which our game servers are listening. */
    public int getServerPort () {
        return _config.getValue("server_port", Client.DEFAULT_SERVER_PORTS[0]);
    }

    /** Returns the directory under which all resources are stored. */
    public File getResourceDir () {
        return new File(requireValue("resource_dir"));
    }

    /** Returns the base URL via which all resources are downloaded. */
    public String getResourceURL () {
        return requireValue("resource_url");
    }

    /** Returns the JDBC configuration for this installation. */
    public Properties getJDBCConfig () {
        return _config.getSubProperties("db");
    }

    /** Helper function for warning on undefined config elements. */
    protected String requireValue (String key) {
        String value = _config.getValue(key, "");
        if (StringUtil.isBlank(value)) {
            log.warning("Missing required configuration '" + key + "'.");
        }
        return value;
    }

    protected final Config _config;
}
