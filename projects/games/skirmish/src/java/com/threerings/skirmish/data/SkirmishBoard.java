//
// $Id: SkirmishBoard.java,v 1.6 2002/07/26 21:53:22 mdb Exp $

package com.threerings.skirmish.data;

import java.util.Random;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.RandomUtil;

/**
 * Provides access to the abstract board representation and manages
 * encoding and decoding of same.
 */
public class SkirmishBoard extends SimpleStreamableObject
    implements SkirmishCodes
{
    /** Board feature constant for an empty cell. */
    public static final byte EMPTY_CELL = 0;

    /** Board feature constant for a whirlpool. */
    public static final byte WHIRLPOOL_CELL = 1;

    /** Board feature constant for a gust of wind to the north. */
    public static final byte NORTH_WIND_CELL = 2;

    /** Board feature constant for a gust of wind to the east. */
    public static final byte EAST_WIND_CELL = 3;

    /** Board feature constant for a gust of wind to the south. */
    public static final byte SOUTH_WIND_CELL = 4;

    /** Board feature constant for a gust of wind to the west. */
    public static final byte WEST_WIND_CELL = 5;

    /** String codes for each of the feature constants. Used when
     * rendering a board as a string. */
    public static final String[] CELL_STRINGS = {
        ".", "@", "^", ">", "v", "<" };

    /** The board width (in cells). */
    public int width;

    /** The board height (in cells). */
    public int height;

    /** The feature information (whirlpools, wind, etc.) for each cell (in
     * row major order). */
    public byte[] features;

    /**
     * Used when unserializing.
     */
    public SkirmishBoard ()
    {
    }

    /**
     * Returns the feature at the specified column and row.
     */
    public byte getFeature (int column, int row)
    {
        return features[row*width+column];
    }

    /**
     * Applies any action associated with the board position of the
     * supplied vessel to that vessel, updating that vessel's position and
     * orientation accordingly.
     *
     * @return true if the vessel's state was modified.
     */
    public boolean apply (SkirmishVessel vessel)
    {
        byte feature = getFeature(vessel.column, vessel.row);

        switch (feature) {
        case NORTH_WIND_CELL:
            vessel.column += DX[NORTH];
            vessel.row += DY[NORTH];
            return true;

        case EAST_WIND_CELL:
            vessel.column += DX[EAST];
            vessel.row += DY[EAST];
            return true;

        case SOUTH_WIND_CELL:
            vessel.column += DX[SOUTH];
            vessel.row += DY[SOUTH];
            return true;

        case WEST_WIND_CELL:
            vessel.column += DX[WEST];
            vessel.row += DY[WEST];
            return true;

        case WHIRLPOOL_CELL:
            // spin them around randomly
            vessel.orient = (byte)
                ((vessel.orient + RandomUtil.getInt(3) + 1) % 4);
            return true;
        }

        return false;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("   ");
        for (int xx = 0; xx < width; xx++) {
            buf.append(xx/10);
        }
        buf.append("\n   ");
        for (int xx = 0; xx < width; xx++) {
            buf.append(xx%10);
        }
        for (int yy = 0; yy < height; yy++) {
            buf.append("\n").append(yy/10).append(yy%10).append(" ");
            for (int xx = 0; xx < width; xx++) {
                buf.append(CELL_STRINGS[getFeature(xx, yy)]);
            }
        }
        return buf.toString();
    }

    /**
     * Generates a random skirmish board with the specified dimesions.
     * Features are placed randomly on the board.
     */
    public static SkirmishBoard generateBoard (
        int width, int height, int featureDensity)
    {
        return generateBoard(
            width, height, System.currentTimeMillis(), featureDensity);
    }

    /**
     * Generates a random skirmish board with the specified dimesions.
     * Features are placed randomly on the board. The supplied seed is
     * used to seed the random number generated used when generating the
     * board.
     */
    public static SkirmishBoard generateBoard (
        int width, int height, long randSeed, int featureDensity)
    {
        SkirmishBoard board = new SkirmishBoard(width, height, randSeed);

        // place 2*sqrt(width*height) whirlpools on the board
        int pcount = (int)Math.sqrt(width*height)*featureDensity/5;
        for (int ii = 0; ii < pcount; ii++) {
            int column = board._rando.nextInt(width);
            int row = board._rando.nextInt(height);
            board.setFeature(column, row, WHIRLPOOL_CELL);
        }

        // place sqrt(width*height) gusts of wind on the board
        int gcount = (int)Math.sqrt(width*height)*featureDensity/10;
        for (int ii = 0; ii < gcount; ii++) {
            int column = board._rando.nextInt(width);
            int row = board._rando.nextInt(height);
            int orient = board._rando.nextInt(4);
            int length = board._rando.nextInt(MAX_GUST_LENGTH) + 1;
            byte feature = (byte)(orient + NORTH_WIND_CELL);

//             System.out.println("Generating gust " +
//                                "[x=" + column + ", y=" + row +
//                                ", orient=" + orient +
//                                ", length=" + length + "].");

            for (int pp = 0; pp < length; pp++) {
                board.setFeature(column, row, feature);
                column = (column + width + DX[orient]) % width;
                row = (row + height + DY[orient]) % height;
            }
        }

        return board;
    }

    /**
     * Used by {@link #generateBoard}.
     */
    protected SkirmishBoard (int width, int height, long randSeed)
    {
        this.width = width;
        this.height = height;
        this.features = new byte[width*height];
        _rando = new Random(randSeed);
    }

    /**
     * Causes the specified cell to contain the specified feature.
     */
    protected void setFeature (int column, int row, byte feature)
    {
        features[row*width+column] = feature;
    }

    /** A random number generator used when generating the board. */
    protected transient Random _rando;

    /** The maximum length of a gust of wind (used when generating a
     * random board). */
    protected static final int MAX_GUST_LENGTH = 6;

    /** Used when adjusting a coordinate in a particular compass
     * direction. */
    public static final int[] DX = { 0, 1, 0, -1 }; // N E S W

    /** Used when adjusting a coordinate in a particular compass
     * direction. */
    public static final int[] DY = { -1, 0, 1, 0 }; // N E S W
}
