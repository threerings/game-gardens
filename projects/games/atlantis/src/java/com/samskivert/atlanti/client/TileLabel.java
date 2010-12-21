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

package com.samskivert.atlanti.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import com.samskivert.atlanti.data.AtlantiTile;
import com.samskivert.atlanti.data.TileCodes;

/**
 * Displays a single tile in a Swing component.
 */
public class TileLabel extends JComponent
    implements TileCodes
{
    /**
     * Configures the component to display the specified tile.
     *
     * @param tile a reference to the tile to display or null if no tile
     * should be displayed.
     */
    public void setTile (AtlantiTile tile)
    {
        _tile = tile;
        repaint();
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // simply paint the tile if we have one
        if (_tile != null) {
            _tile.paint((Graphics2D)g, 0, 0);
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(TILE_WIDTH, TILE_HEIGHT);
    }

    /** The tile we are displaying. */
    protected AtlantiTile _tile;
}
