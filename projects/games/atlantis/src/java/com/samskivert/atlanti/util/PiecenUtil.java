//
// Atlantis - A tile laying game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/atlantis/LICENSE

package com.samskivert.atlanti.util;

import java.awt.Image;

import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.UniformTileSet;

public class PiecenUtil
{
    /**
     * Loads up the piecen images using the supplied tile manager.
     */
    public static void init (TileManager tmgr)
    {
        UniformTileSet piecenSet = tmgr.loadTileSet(
            PIECEN_IMG_PATH, PIECEN_WIDTH, PIECEN_HEIGHT);
        _images = new Image[PIECEN_TYPES];
        for (int ii = 0; ii < PIECEN_TYPES; ii++) {
            _images[ii] = piecenSet.getRawTileImage(ii);
        }
    }

    /**
     * Returns the piecen image for the specified piecen color.
     */
    public static Image getPiecenImage (int color)
    {
        return _images[color];
    }

    /** Our piecen images. */
    protected static Image[] _images;

    /** The number of different colors of piecen. */
    protected static final int PIECEN_TYPES = 6;

    /** The width of the piecen image in pixels. */
    protected static final int PIECEN_WIDTH = 16;

    /** The height of the piecen image in pixels. */
    protected static final int PIECEN_HEIGHT = 16;

    /** The path to the piecen image (relative to the resource
     * directory). */
    protected static final String PIECEN_IMG_PATH = "media/piecens.png";
}
