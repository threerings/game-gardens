//
// $Id: SkirmishCodes.java,v 1.6 2002/07/12 23:45:03 mdb Exp $

package com.threerings.skirmish.data;

/**
 * Codes and constants used to implement the game.
 */
public interface SkirmishCodes
{
    /** The message bundle identifier for translation messages. */
    public static final String SKIRMISH_MESSAGE_BUNDLE = "skirmish";

    /** Used when adjusting a coordinate in a particular compass
     * direction. */
    public static final int[] DX = { 0, 1, 0, -1 }; // N E S W

    /** Used when adjusting a coordinate in a particular compass
     * direction. */
    public static final int[] DY = { -1, 0, 1, 0 }; // N E S W

    /** Direction constant used for vessel orientation. */
    public static final int NORTH = 0;

    /** Direction constant used for vessel orientation. */
    public static final int EAST = 1;

    /** Direction constant used for vessel orientation. */
    public static final int SOUTH = 2;

    /** Direction constant used for vessel orientation. */
    public static final int WEST = 3;

    /** The distance traveled by a cannon when fired. */
    public static final int CANNON_FIRE_DISTANCE = 3;

    /** A message submitted to the server when they have modified their
     * hand. */
    public static final String SET_HAND_REQUEST = "set_hand";
}
