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

package com.samskivert.atlanti.util;

import java.awt.Image;

import com.threerings.media.tile.Tile;
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
        for (int i = 0; i < PIECEN_TYPES; i++) {
            _images[i] = piecenSet.getRawTileImage(i);
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
