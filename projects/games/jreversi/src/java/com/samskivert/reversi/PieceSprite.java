//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/jreversi/LICENSE

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
