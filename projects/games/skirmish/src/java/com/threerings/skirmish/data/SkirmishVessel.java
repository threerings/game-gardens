//
// $Id: SkirmishVessel.java,v 1.5 2002/07/24 22:38:45 mdb Exp $

package com.threerings.skirmish.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Represents a vessel on the skirmish board.
 */
public class SkirmishVessel extends SimpleStreamableObject
    implements Cloneable, SkirmishCodes
{
    /** The row occupied by the vessel. */
    public byte column;

    /** The column occupied by the vessel. */
    public byte row;

    /** The orientation of the vessel. */
    public byte orient;

    /** The direction this vessel is shooting, if it is doing so. */
    public byte shotOrient = -1;

    /** The distance traveled by the shot or -1 if the shot did not strike
     * a target but splashed down instead (in which case the shot will
     * travel the maximum distance). */
    public byte shotDist = -1;

    /**
     * Constructs a skirmish vessel and initializes its position and
     * orientation.
     */
    public SkirmishVessel (int column, int row, int orient)
    {
        this.column = (byte)column;
        this.row = (byte)row;
        this.orient = (byte)orient;
    }

    /**
     * Used when unserializing.
     */
    public SkirmishVessel ()
    {
    }

    /**
     * Generates a deep, hard and fast clone of this vessel instance.
     */
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("What, no clone?");
        }
    }

    /**
     * Returns the orientation the source vessel would give to its cannons
     * to fire at the target vessel.
     */
    public static int cannonOrient (SkirmishVessel source,
                                    SkirmishVessel target)
    {
        if (source.orient == NORTH || source.orient == SOUTH) {
            return (target.column < source.column) ? WEST : EAST;
        } else {
            return (target.row < source.row) ? NORTH : SOUTH;
        }
    }
}
