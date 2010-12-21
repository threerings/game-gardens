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

import java.awt.Point;
import javax.swing.JComponent;

import com.threerings.media.util.LinePath;

import com.threerings.toybox.util.GameViewTest;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * A test harness for our board view.
 */
public class ReversiBoardViewTest extends GameViewTest
{
    public static void main (String[] args)
    {
        ReversiBoardViewTest test = new ReversiBoardViewTest();
        test.display();
    }

    protected JComponent createInterface (ToyBoxContext ctx)
    {
        return _view = new ReversiBoardView(ctx, new ReversiController());
    }

    protected void initInterface ()
    {
        // add a couple of pieces to the view
        ReversiObject.Piece piece = new ReversiObject.Piece();
        piece.owner = ReversiObject.BLACK;
        piece.x = 1;
        piece.y = 2;
        _view.addSprite(new PieceSprite(piece));

        piece = new ReversiObject.Piece();
        piece.owner = ReversiObject.WHITE;
        piece.x = 2;
        piece.y = 2;

        PieceSprite sprite = new PieceSprite(piece);
        _view.addSprite(sprite);
        int dx = piece.x * PieceSprite.SIZE;
        int dy = (piece.y+1) * PieceSprite.SIZE;
        sprite.move(new LinePath(new Point(dx, dy), 1000L));

        _view.setPlacingMode(ReversiObject.WHITE);
    }

    protected ReversiBoardView _view;
}
