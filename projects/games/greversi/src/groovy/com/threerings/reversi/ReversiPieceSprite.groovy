//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

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
