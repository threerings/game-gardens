//
// $Id$

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

    public void setPosition (int x, int y)
    {
        _piece.x = x;
        _piece.y = y;
        updatePiece(_piece);
    }

    public ReversiObject.Piece getPiece ()
    {
        return _piece;
    }

    @Override // from PieceSprite
    public void paint (Graphics2D gfx)
    {
        Composite ocomp = gfx.getComposite();
        gfx.setComposite(_comp);
        super.paint(gfx);
        gfx.setComposite(ocomp);
    }

    protected AlphaComposite _comp;
}
