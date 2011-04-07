//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.reversi;

import com.threerings.presents.dobj.DSet;

/**
 * Represents a particular piece on the board.
 */
public class ReversiPiece
    implements DSet.Entry
{
    /** Represents a black piece. */
    public static int BLACK = 0;

    /** Represents a white piece. */
    public static int WHITE = 1;

    /** The color of this piece. */
    public int color;

    /** This piece's coordinates. */
    public int x, y;

    // from interface DSet.Entry
    public Comparable getKey ()
    {
        return (x << 8) | y;
    }
}
