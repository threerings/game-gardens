//
// $Id$

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
        return _view = new ReversiBoardView(ctx);
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
    }

    protected ReversiBoardView _view;
}
