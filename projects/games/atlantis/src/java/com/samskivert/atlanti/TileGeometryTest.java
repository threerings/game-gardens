//
// Atlantis - A tile laying game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/atlantis/LICENSE

package com.samskivert.atlanti;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.TileManager;
import com.threerings.resource.ResourceManager;

import com.samskivert.atlanti.data.AtlantiTile;
import com.samskivert.atlanti.data.TileCodes;
import com.samskivert.atlanti.util.PiecenUtil;

/**
 * A simple class for testing the tile geometry specifications by drawing
 * them.
 */
public class TileGeometryTest extends JPanel
    implements TileCodes
{
    public TileGeometryTest ()
    {
        for (int ii = 0; ii < TILE_TYPES; ii++) {
            _tiles[ii] = new AtlantiTile(ii+1, true, NORTH, ii % 5, ii / 5);
        }
    }

    @Override
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // paint our tiles
        for (AtlantiTile tile : _tiles) {
            tile.paint((Graphics2D)g, 0, 0);
        }
    }

    @Override
    public Dimension getPreferredSize ()
    {
        // we want to be five tiles wide by four tiles tall
        return new Dimension(TILE_WIDTH * 5, TILE_HEIGHT * 4);
    }

    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Tile geometry test");

        ResourceManager rmgr = new ResourceManager("rsrc");
        ImageManager imgr = new ImageManager(rmgr, frame);
        TileManager tmgr = new TileManager(imgr);

        AtlantiTile.setManagers(imgr, tmgr);
        AtlantiTile.piecenDebug = true;
        PiecenUtil.init(tmgr);

        // quit if we're closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TileGeometryTest panel = new TileGeometryTest();
        frame.getContentPane().add(panel);
        frame.pack();
        SwingUtil.centerWindow(frame);
        frame.setVisible(true);
    }

    protected AtlantiTile[] _tiles = new AtlantiTile[TILE_TYPES];
}
