//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.client;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import com.threerings.media.MediaPanel;
import com.threerings.media.image.BufferedMirage;
import com.threerings.media.sprite.ImageSprite;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.toybox.util.ToyBoxContext;

import com.samskivert.sagashi.data.SagashiBoard;
import com.samskivert.sagashi.data.SagashiObject;

/**
 * Displays the Sagashi board.
 */
public class SagashiBoardView extends MediaPanel
    implements PlaceView, AttributeChangeListener
{
    /**
     * Constructs and initializes the view which display the Sagashi
     * board.
     */
    public SagashiBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;

        // load up our letter images
        _images = new BufferedMirage[SagashiBoard.LETTERS];

        BufferedImage source = _ctx.loadImage("media/letters.png");
        int perRow = source.getWidth() / TILE_SIZE;
        for (int ii = 0; ii < _images.length; ii++) {
            int xx = ii % perRow, yy = ii / perRow;
            _images[ii] = new BufferedMirage(
                source.getSubimage(xx * TILE_SIZE, yy * TILE_SIZE,
                                   TILE_SIZE, TILE_SIZE));
        }
    }

    @Override // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(6 * TILE_SIZE, 6 * TILE_SIZE);
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _sagaobj = (SagashiObject)plobj;
        _sagaobj.addListener(this);

        // update the board display
        updateBoard();
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_sagaobj != null) {
            _sagaobj.removeListener(this);
            _sagaobj = null;
        }
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (SagashiObject.BOARD.equals(event.getName())) {
            updateBoard();
        }
    }

    /**
     * Clears out and recreates the sprites used to display our board.
     */
    protected void updateBoard ()
    {
        clearSprites();

        // nothing to do until we have a board
        if (_sagaobj.board == null) {
            return;
        }

        int size = _sagaobj.board.getSize(), count = size * size;
        int offset = (6-size)*TILE_SIZE/2;
        for (int ii = 0; ii < count; ii++) {
            int xx = ii % size, yy = ii / size;
            int code = _sagaobj.board.getLetterCode(ii);
            ImageSprite sprite = new ImageSprite(_images[code]);
            sprite.setLocation(offset + xx * TILE_SIZE,
                               offset + yy * TILE_SIZE);
            addSprite(sprite);
        }
    }

    /** Provides access to client services. */
    protected ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected SagashiObject _sagaobj;

    /** Contains our letter images. */
    protected BufferedMirage[] _images;

    /** The letter tiles are 64 x 64. */
    protected static final int TILE_SIZE = 64;
}
