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
