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

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.media.sprite.Sprite;

/**
 * Displays a Reversi piece on the board.
 */
public class ReversiPieceSprite extends Sprite
{
    /** The size of our sprite (on a side) in pixels. */
    public static final int SIZE = 32;

    public ReversiPieceSprite (ReversiPiece piece)
    {
        super(32, 32); // if we reference SIZE here it results in a verifier
                       // error
        _piece = piece;
        pieceUpdated();
    }

    public void pieceUpdated ()
    {
        setLocation(SIZE * _piece.x, SIZE * _piece.y);
        invalidate();
    }

    // @Override // from Sprite
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(COLORS[_piece.color]);
        gfx.fillOval(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
        gfx.setColor(Color.black);
        gfx.drawOval(_bounds.x, _bounds.y, _bounds.width-1, _bounds.height-1);
    }

    protected ReversiPiece _piece;

    protected static final def COLORS = [ Color.black, Color.white ];
}
