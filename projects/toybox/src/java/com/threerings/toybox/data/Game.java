//
// $Id: Game.java,v 1.1 2004/01/20 14:44:40 mdb Exp $

package com.threerings.toybox.data;

import com.samskivert.util.StringUtil;

/**
 * Contains information about a game registration.
 */
public class Game
{
    /** A {@link #status} constant. */
    public static final int UNKNOWN = 0;

    /** A {@link #status} constant. */
    public static final int PENDING = 1;

    /** A {@link #status} constant. */
    public static final int PUBLISHED = 2;

    /** A {@link #status} constant. */
    public static final int DISABLED = 3;

    /** The unique identifier for this game. */
    public int gameId;

    /** The user id of the maintainer of this game. */
    public int maintainerId;

    /** The status of the game: {@link #PENDING}, {@link #PUBLISHED}, etc. */
    public int status;

    /** The server on which this game is hosted. */
    public String host;

    /** The human readable name of this game. */
    public String name;

    /** The XML game definition associated with this version. */
    public String definition;

    /** The XML game definition associated with the test version. */
    public String testDefinition;

    /**
     * Provides a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
