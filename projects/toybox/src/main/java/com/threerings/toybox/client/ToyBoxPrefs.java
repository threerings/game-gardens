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

package com.threerings.toybox.client;

import java.awt.Rectangle;

import com.samskivert.util.PrefsConfig;
import com.samskivert.util.StringUtil;

/**
 * Maintains preferences for the ToyBox client.
 */
public class ToyBoxPrefs
{
    /** Defines the location in the preference system of our prefs. */
    public static PrefsConfig config = new PrefsConfig("com.threerings.toybox");

    /**
     * Returns the preferred bounds for the client frame.
     *
     * @param gameId the id of the game we are playing.
     * @param username the developer override username if one was provided
     * or null otherwise.
     *
     * @return null if no saved bounds exist for this game and user
     * combination or a rectangle indicating the bounds if they do.
     */
    public static Rectangle getClientBounds (String gameId, String username)
    {
        int[] v = config.getValue(makeKey(gameId, username), (int[])null);
        return v == null ? null : new Rectangle(v[0], v[1], v[2], v[3]);
    }

    /**
     * Updates our stored setting of the preferred bounds for the client
     * frame.
     *
     * @param gameId the id of the game we are playing.
     * @param username the developer override username if one was provided
     * or null otherwise.
     * @param bounds the new frame bounds.
     */
    public static void setClientBounds (
        String gameId, String username, Rectangle bounds)
    {
        int[] v = new int[] { bounds.x, bounds.y, bounds.width, bounds.height };
        config.setValue(makeKey(gameId, username), v);
    }

    /**
     * Returns the user's username.
     */
    public static String getUsername ()
    {
        return config.getValue(USERNAME_KEY, "");
    }

    /**
     * Sets the user's username.
     */
    public static void setUsername (String value)
    {
        config.setValue(USERNAME_KEY, value);
    }

    /**
     * Returns the user's (encrypted) password.
     */
    public static String getPassword ()
    {
        return config.getValue(PASSWORD_KEY, "");
    }

    /**
     * Sets the user's (encrypted) password.
     */
    public static void setPassword (String value)
    {
        config.setValue(PASSWORD_KEY, value);
    }

    /**
     * Gets the length of the unencrypted password.
     */
    public static int getPasswordLength ()
    {
        return config.getValue(PASSWORD_LEN_KEY, 0);
    }

    /**
     * Sets the length of the unencrypted password.
     */
    public static void setPasswordLength (int len)
    {
        config.setValue(PASSWORD_LEN_KEY, len);
    }

    /**
     * Returns whether we're remembering the user's password.
     */
    public static boolean getRememberPassword ()
    {
        return config.getValue(REMEMBER_PASSWORD_KEY, false);
    }

    /**
     * Sets whether we're remembering the user's password.
     */
    public static void setRememberPassword (boolean value)
    {
        config.setValue(REMEMBER_PASSWORD_KEY, value);
    }

    /** A helper function. */
    protected static String makeKey (String gameId, String username)
    {
        String key = "client_bounds." + gameId;
        if (!StringUtil.isBlank(username)) {
            key += "." + username;
        }
        return key;
    }

    /** The configuration key for the user's username. */
    protected static final String USERNAME_KEY = "username";

    /** The configuration key for the user's password. */
    protected static final String PASSWORD_KEY = "password";

    /** The configuration key for the user's password length. */
    protected static final String PASSWORD_LEN_KEY = "password_len";

    /** The configuration key for whether the user's password is remembered. */
    protected static final String REMEMBER_PASSWORD_KEY = "remember_password";
}
