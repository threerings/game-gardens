//
// $Id: SkirmishAction.java,v 1.6 2002/08/15 23:24:29 mdb Exp $

package com.threerings.skirmish.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Represents an action that is used by a player to move their vessel or
 * fire, etc.
 */
public class SkirmishAction extends SimpleStreamableObject
    implements SkirmishCodes
{
    /** Action code for a no op action. */
    public static final byte NOOP_ACTION = 0;

    /** Action code for a move forward action. */
    public static final byte FORWARD_ACTION = 1;

    /** Action code for a turn left action. */
    public static final byte TURN_LEFT_ACTION = 2;

    /** Action code for a turn right action. */
    public static final byte TURN_RIGHT_ACTION = 3;

    /** Action code for a fire action. */
    public static final byte FIRE_ACTION = 4;

    /** An action singleton for {@link #NOOP_ACTION}. */
    public static final SkirmishAction NOOP = new SkirmishAction(NOOP_ACTION);

    /** An action singleton for {@link #FORWARD_ACTION}. */
    public static final SkirmishAction FORWARD =
        new SkirmishAction(FORWARD_ACTION);

    /** An action singleton for {@link #TURN_LEFT_ACTION}. */
    public static final SkirmishAction TURN_LEFT =
        new SkirmishAction(TURN_LEFT_ACTION);

    /** An action singleton for {@link #TURN_RIGHT_ACTION}. */
    public static final SkirmishAction TURN_RIGHT =
        new SkirmishAction(TURN_RIGHT_ACTION);

    /** An action singleton for {@link #FIRE_ACTION}. */
    public static final SkirmishAction FIRE = new SkirmishAction(FIRE_ACTION);

    /** The number of action types that can exist in the action cache. */
    public static final int CACHED_COUNT = (FIRE_ACTION - FORWARD_ACTION + 1);

    /** The code representing the kind of action. */
    public byte code;

    /**
     * Constructs an action instance.
     */
    public SkirmishAction (byte code)
    {
        this.code = code;
    }

    /**
     * Used when unserializing.
     */
    public SkirmishAction ()
    {
    }

    /**
     * Applies this action to the supplied vessel, updating that vessel's
     * position and orientation accordingly.
     *
     * @return true if the vessel's state was modified.
     */
    public boolean apply (SkirmishVessel vessel)
    {
        // only certain actions have an impact on the vessel's position
        // and orientation
        switch (code) {
        case FORWARD_ACTION:
            vessel.column += DX[vessel.orient];
            vessel.row += DY[vessel.orient];
            return true;

        case TURN_LEFT_ACTION:
            vessel.column += DX[vessel.orient];
            vessel.row += DY[vessel.orient];
            vessel.orient = (byte)((vessel.orient + 3) % 4);
            vessel.column += DX[vessel.orient];
            vessel.row += DY[vessel.orient];
            return true;

        case TURN_RIGHT_ACTION:
            vessel.column += DX[vessel.orient];
            vessel.row += DY[vessel.orient];
            vessel.orient = (byte)((vessel.orient + 1) % 4);
            vessel.column += DX[vessel.orient];
            vessel.row += DY[vessel.orient];
            return true;
        }

        return false;
    }

    /**
     * Returns the index into the cache array to be used for an action of
     * this type.
     */
    public static int toIndex (byte actionType)
    {
        return (actionType - FORWARD_ACTION);
    }

    /**
     * Returns the action type that is associated with this cache array
     * index.
     */
    public static byte fromIndex (int index)
    {
        return (byte)(index + FORWARD_ACTION);
    }
}
