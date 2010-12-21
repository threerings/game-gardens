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

package com.samskivert.reversi;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threerings.media.sprite.Sprite;

/**
 * Displays a piece on the board view.
 */
public class PieceSprite extends Sprite
{
    /** The dimensions of our sprite in pixels. */
    public static final int SIZE = 64;

    /**
     * Creates a piece sprite to display the supplied game piece.
     */
    public PieceSprite (ReversiObject.Piece piece)
    {
        super(SIZE, SIZE);
        updatePiece(piece);
    }

    /**
     * Called when the piece we are displaying has been updated.
     */
    public void updatePiece (ReversiObject.Piece piece)
    {
        // keep track of our piece
        _piece = piece;

        // set our location based on the location of the piece
        setLocation(_piece.x * SIZE, _piece.y * SIZE);

        // force a redraw in case our color changed but not our location
        invalidate();
    }

    @Override // from Sprite
    public void paint (Graphics2D gfx)
    {
        // set our color depending on the player that owns this piece
        gfx.setColor(_piece.owner == ReversiObject.BLACK ?
                     Color.darkGray : Color.white);

        // draw a filled in circle in our piece color
        int px = _bounds.x + 3, py = _bounds.y + 3;
        int pwid = _bounds.width - 6, phei = _bounds.height - 6;
        gfx.fillOval(px, py, pwid, phei);

        // then outline that oval in black
        gfx.setColor(Color.black);
        gfx.drawOval(px, py, pwid, phei);
    }

    protected ReversiObject.Piece _piece;
}
