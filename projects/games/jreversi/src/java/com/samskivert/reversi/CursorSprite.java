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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * Displays a "potential move" cursor to the player.
 */
public class CursorSprite extends PieceSprite
{
    public CursorSprite ()
    {
        super(new ReversiObject.Piece());
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    }

    public void setColor (int color)
    {
        _piece.owner = color;
    }

    public void setPosition (int x, int y, ReversiLogic logic)
    {
        _piece.x = x;
        _piece.y = y;
        _legal = logic.isLegalMove(_piece);
        updatePiece(_piece);
    }

    public ReversiObject.Piece getPiece ()
    {
        return _piece;
    }

    @Override // from PieceSprite
    public void paint (Graphics2D gfx)
    {
        if (_legal) {
            Composite ocomp = gfx.getComposite();
            gfx.setComposite(_comp);
            super.paint(gfx);
            gfx.setComposite(ocomp);
        }
    }

    protected AlphaComposite _comp;
    protected boolean _legal = false;
}
