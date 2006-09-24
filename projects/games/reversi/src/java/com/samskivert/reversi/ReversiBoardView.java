//
// $Id$

package com.samskivert.reversi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.media.VirtualMediaPanel;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

/**
 * Displays the main game interface (the board).
 */
public class ReversiBoardView extends VirtualMediaPanel
    implements PlaceView, SetListener
{
    /**
     * Constructs a view which will initialize itself and prepare to display
     * the game board.
     */
    public ReversiBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
    }

    // from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
        _gameobj = (ReversiObject)plobj;
        _gameobj.addListener(this);

        // create sprites for all pieces currently on the board
        for (ReversiObject.Piece piece : _gameobj.pieces) {
            addPieceSprite(piece);
        }
    }

    // from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
        _gameobj.removeListener(this);
        _gameobj = null;
    }

    // from interface SetListener
    public void entryAdded (EntryAddedEvent event)
    {
        if (event.getName().equals(ReversiObject.PIECES)) {
            // add a sprite for the newly created piece
            addPieceSprite((ReversiObject.Piece)event.getEntry());
        }
    }

    // from interface SetListener
    public void entryUpdated (EntryUpdatedEvent event)
    {
        if (event.getName().equals(ReversiObject.PIECES)) {
            // update the sprite that is displaying the updated piece
            ReversiObject.Piece piece = (ReversiObject.Piece)event.getEntry();
            _sprites.get(piece.getKey()).updatePiece(piece);
        }
    }

    // from interface SetListener
    public void entryRemoved (EntryRemovedEvent event)
    {
        // nothing to do here
    }

    @Override // from JComponent
    public Dimension getPreferredSize ()
    {
        return new Dimension(_size.width * PieceSprite.SIZE + 1,
                             _size.height * PieceSprite.SIZE + 1);
    }

    /**
     * Adds a sprite to the board for the supplied piece.
     */
    protected void addPieceSprite (ReversiObject.Piece piece)
    {
        PieceSprite sprite = new PieceSprite(piece);
        _sprites.put(piece.getKey(), sprite);
        addSprite(sprite);
    }

    @Override // from MediaPanel
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);

        // fill in our background color
        gfx.setColor(Color.lightGray);
        gfx.fill(dirtyRect);

        // draw our grid
        gfx.setColor(Color.black);
        for (int yy = 0; yy <= _size.height; yy++) {
            int ypos = yy * PieceSprite.SIZE;
            gfx.drawLine(0, ypos, PieceSprite.SIZE * _size.width, ypos);
        }
        for (int xx = 0; xx <= _size.width; xx++) {
            int xpos = xx * PieceSprite.SIZE;
            gfx.drawLine(xpos, 0, xpos, PieceSprite.SIZE * _size.height);
        }
    }

    /** Provides access to client services. */
    protected ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected ReversiObject _gameobj;

    /** The size of the Reversi board. */
    protected Dimension _size = new Dimension(8, 8);

    /** Contains a mapping from piece id to the sprite for that piece. */
    protected HashMap<Comparable,PieceSprite> _sprites =
        new HashMap<Comparable,PieceSprite>();
}
