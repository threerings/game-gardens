//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
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
