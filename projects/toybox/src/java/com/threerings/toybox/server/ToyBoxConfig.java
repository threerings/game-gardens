//
// $Id: ToyBoxConfig.java,v 1.1 2004/01/20 14:44:40 mdb Exp $

package com.threerings.toybox.server;

import java.io.File;
import java.util.Properties;

import com.samskivert.util.Config;

import com.threerings.toybox.Log;
import com.threerings.toybox.data.Game;
import com.threerings.toybox.data.ToyBoxCodes;

/**
 * Provides access to installation specific configuration parameters. Once
 * the {@link ToyBoxManager} has been created, the {@link ToyBoxConfig} is
 * initialized and becomes available for use.
 */
public class ToyBoxConfig
    implements ToyBoxCodes
{
    /** Provides access to our config properties. <em>Do not</em> modify
     * these properties! */
    public static Config config;

    /**
     * Creates our {@link Config} instance. This method is called by the
     * {@link ToyBoxManager} when it is constructed. If this is to be used
     * in circumstances where there is no {@link ToyBoxManager} it can be
     * initialized by hand with the proper configuration file.
     */
    public static void init (Properties props)
    {
        if (config == null) {
            config = new Config("toybox", props);
        }
    }

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
}
