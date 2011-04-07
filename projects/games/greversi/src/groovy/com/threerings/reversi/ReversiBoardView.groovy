//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.reversi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.media.VirtualMediaPanel;

import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.groovy.client.ClosureMouseMovedAdapter;

/**
 * Displays the board and allows the player to make a move.
 */
public class ReversiBoardView extends VirtualMediaPanel
    implements PlaceView
{
    public ReversiBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;

        addMouseMotionListener(new ClosureMouseMovedAdapter(closure:{
            mouseMoved(it.getX(), it.getY());
        }));
    }

    /**
     * Called by the controller to activate and deactivate placing mode on our
     * turn.
     */
    public void setPlacing (boolean placingMode)
    {
        if (placingMode) {
            if (_placer == null) {
                addSprite(_placer = new PlacingSprite(_ppiece));
            }
        } else {
            if (_player != null) {
                removeSprite(_placer);
                _placer = null;
            }
        }
    }

    public void mouseMoved (int mx, int my)
    {
        // keep our "placing" piece up updated with the latest mouse coords
        int px = Math.floor(mx/ReversiPieceSprite.SIZE);
        int py = Math.floor(my/ReversiPieceSprite.SIZE);
        px = Math.max(0, Math.min(px, ReversiObject.BOARD_SIZE));
        py = Math.max(0, Math.min(py, ReversiObject.BOARD_SIZE));
        _ppiece.x = px;
        _ppiece.y = py;

        // if we're actually placing, update the sprite
        if (_placer != null) {
            _placer.pieceUpdated();
        }
    }

    // from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
        _revobj = plobj;
    }

    // from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    // @Override // from MediaPanel
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);

        // draw our grid
        int totside = ReversiPieceSprite.SIZE * ReversiObject.BOARD_SIZE;
        gfx.setColor(Color.black);
        for (yy in 0 .. ReversiObject.BOARD_SIZE+1) {
            int cy = yy * ReversiPieceSprite.SIZE;
            gfx.drawLine(0, cy, totside + ReversiPieceSprite.SIZE, cy);
        }
        for (xx in 0 .. ReversiObject.BOARD_SIZE+1) {
            int cx = xx * ReversiPieceSprite.SIZE;
            gfx.drawLine(cx, 0, cx, totside + ReversiPieceSprite.SIZE);
        }
    }

    protected ToyBoxContext _ctx;
    protected ReversiObject _revobj;

    protected ReversiPiece _ppiece = new ReversiPiece();
    protected PlacingSprite _placer;
}
