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
import java.util.Properties;

import com.samskivert.util.Config;

import com.threerings.toybox.Log;
import com.threerings.toybox.server.persist.Game;

import static com.threerings.toybox.data.ToyBoxCodes.*;

/**
 * Provides access to installation specific configuration parameters. Once
 * the {@link ToyBoxManager} has been created, the {@link ToyBoxConfig} is
 * initialized and becomes available for use.
 */
public class ToyBoxConfig
{
    /** Provides access to our config properties. <em>Do not</em> modify
     * these properties! */
    public static Config config = new Config("toybox");

    /**
     * Returns the directory in which game data is to be stored.
     */
    public static File getDataDirectory ()
    {
        return new File(config.getValue("game_data_dir",
                                        System.getProperty("java.io.tmpdir")));
    }

    /**
     * Returns the directory in which standard libraries are stored.
     */
    public static File getLibraryDirectory ()
    {
        return new File(getDataDirectory(), LIBRARY_SUBDIR);
    }

    /**
     * Returns the name of the {@link #getDataDirectory} sub-directory
     * which contains data for the specified game. <em>Note:</em> this is
     * not the full path, use {@link #getGameDirectory} for that.
     */
    public static String getGameSubdir (Game game)
    {
        return "" + game.gameId;
    }

    /**
     * Returns the directory in which the resources for the specified game
     * reside. The directory will be created if it does not yet exist.
     */
    public static File getGameDirectory (Game game)
    {
        File gdir = new File(getDataDirectory(), getGameSubdir(game));
        if (!gdir.exists()) {
            gdir.mkdir();
        }
        if (!gdir.isDirectory()) {
            Log.warning("Unable to create game directory (" + gdir.getPath() +
                        "). Please check permissions, etc.");
            // nothing else we can do but hope for the best
        }
        return gdir;
    }

    /**
     * Returns the JDBC configuration for this installation.
     */
    public static Properties getJDBCConfig ()
    {
        return config.getSubProperties("db");
    }
}
