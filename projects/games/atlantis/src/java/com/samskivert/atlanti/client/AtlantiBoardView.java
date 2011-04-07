//
// Atlantis - A tile laying game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/atlantis/LICENSE

package com.samskivert.atlanti.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.CollectionUtil;

import com.threerings.media.util.MathUtil;
import com.threerings.presents.dobj.DSet;

import com.samskivert.atlanti.data.AtlantiCodes;
import com.samskivert.atlanti.data.AtlantiTile;
import com.samskivert.atlanti.data.Piecen;
import com.samskivert.atlanti.data.TileCodes;
import com.samskivert.atlanti.util.TileUtil;

import static com.samskivert.atlanti.Log.log;

/**
 * Displays the tiles that make up the board.
 */
public class AtlantiBoardView extends JPanel
    implements TileCodes, AtlantiCodes
{
    /**
     * Constructs a board.
     */
    public AtlantiBoardView (AtlantiController ctrl)
    {
        _ctrl = ctrl;

        // create mouse adapters that will let us know when interesting mouse events happen
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent evt) {
                AtlantiBoardView.this.mouseClicked(evt);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved (MouseEvent evt) {
                AtlantiBoardView.this.mouseMoved(evt);
            }
        });
    }

    /**
     * Sets the piecen color to use when creating new piecens.
     */
    public void setNewPiecenColor (int color)
    {
        _newPiecenColor = color;
    }

    /**
     * Called when we first enter the game room and subsequently if {@link AtlantiObject#TILES} is
     * set.
     */
    public void tilesChanged (DSet<AtlantiTile> tset)
    {
        // clear out our old tiles list
        _tiles.clear();

        // copy the tiles from the set into our local list
        CollectionUtil.addAll(_tiles, tset.iterator());

        // sort the list
        Collections.sort(_tiles);

        // recompute our desired dimensions and then have our parent adjust to our changed size
        computeDimensions();
    }

    /**
     * Called when we first enter the game room and subsequently if {@link AtlantiObject#PIECENS}
     * is set.
     */
    public void piecensChanged (DSet<Piecen> piecens)
    {
        //  just iterate over the set placing each of the piecens in turn
        for (Piecen piecen : piecens) {
            piecensAdded(piecen);
        }
    }

    /**
     * Called when an entry is added to {@link AtlantiObject#TILES}.
     */
    public void tilesAdded (AtlantiTile tile)
    {
        log.info("Adding tile to board " + tile + ".");

        // if we add a tile that is the same as our most recently placed
        // tile, leave the placed tile. otherwise clear it out
        if (!tile.equals(_placedTile)) {
            _placedTile = null;
        }

        // add the tile
        _tiles.add(tile);

        // reference this as our most recently placed tile
        _lastPlacedTile = tile;

        // resort the list
        Collections.sort(_tiles);

        // have the new tile inherit its claim groups
        TileUtil.inheritClaims(_tiles, tile);

        // recompute our desired dimensions and then have our parent adjust to our changed size
        computeDimensions();
    }

    /**
     * Called when an entry is added to {@link AtlantiObject#PIECENS}.
     */
    public void piecensAdded (Piecen piecen)
    {
        // if we still have a placed tile, we get rid of it
        _placedTile = null;

        log.info("Placing " + piecen + ".");

        // locate the tile associated with this piecen
        int tidx = _tiles.indexOf(new AtlantiTile(piecen.x, piecen.y));
        if (tidx != -1) {
            AtlantiTile tile = _tiles.get(tidx);
            // set the piecen on the tile (supplying our tile list so that
            // the necessary claim group adjustments can be made)
            tile.setPiecen(piecen, _tiles);
            // and repaint
            repaintTile(tile);

        } else {
            log.warning("Requested to place piecen for which we could find no associated tile!",
                "piecen", piecen);
        }
    }

    /**
     * Called when an entry is removed from {@link AtlantiObject#PIECENS}.
     */
    public void piecensRemoved (Object key)
    {
        // locate the tile associated with this piecen key
        for (AtlantiTile tile : _tiles) {
            if (tile.getKey().equals(key)) {
                // clear the piecen out of the tile
                tile.clearPiecen();
                // and repaint
                repaintTile(tile);
                // and get on out
                return;
            }
        }

        log.warning("Requested to clear piecen for which we could find no associated tile!",
            "key", key);
    }

    /**
     * Turn off the ability to place a piecen on the most recently played tile. This function
     * assumes that the mouse will not be over a valid piecen placement at the time this function
     * is called (it expects that it will be over a button of some sort that says something to the
     * effect of "skip placement for this turn").
     */
    public void cancelPiecenPlacement ()
    {
        _placingPiecen = false;
    }

    /**
     * If we freed up a placeable piecen that we didn't have when we placed our tile, this can be
     * called to reenable piecen placement.
     */
    public void enablePiecenPlacement ()
    {
        _placingPiecen = true;
    }

    /**
     * Sets the tile to be placed on the board. The tile will be displayed in the square under the
     * mouse cursor where it can be legally placed and its orientation will be determined based on
     * the pointer's proximity to the edges of the target square. When the user clicks the mouse
     * while the tile is in a placeable position, a {@link AtlantiController#tilePlaced} command
     * will be dispatched. The coordinates and orientation of the tile will be available by
     * fetching the tile back via {@link #getPlacedTile}. The tile provided to this method will
     * not be modified.
     *
     * @param tile the new tile to be placed or null if no tile is to currently be placed.
     */
    public void setTileToBePlaced (AtlantiTile tile)
    {
        log.info("Setting tile to be placed", "tile", tile);

        // make a copy of this tile so that we can play with it
        _placingTile = tile.clone();;

        // update our internal state based on this new placing tile
        if (_placingTile != null) {
            updatePlacingInfo(true);
        }
    }

    /**
     * Returns the last tile placed by the user.
     */
    public AtlantiTile getPlacedTile ()
    {
        return _placedTile;
    }

    @Override
    public void doLayout ()
    {
        super.doLayout();

        // compute our translation coordinates based on our size
        _tx = (getWidth() - TILE_WIDTH * _width)/2;
        _ty = (getHeight() - TILE_HEIGHT * _height)/2;
    }

    /**
     * Causes the supplied tile to be repainted.
     */
    public void repaintTile (AtlantiTile tile)
    {
        int offx = _tx + (tile.x + _origX) * TILE_WIDTH;
        int offy = _ty + (tile.y + _origY) * TILE_HEIGHT;
        repaint(offx, offy, TILE_WIDTH, TILE_HEIGHT);
    }

    @Override
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        // center the tile display if we are bigger than we need to be
        g.translate(_tx, _ty);

//         // paint coordinates in the grid that contains our tiles
//         for (int yy = 0; yy < _height; yy++) {
//             for (int xx = 0; xx < _width; xx++) {
//                 int cx = xx*TILE_WIDTH, cy = yy*TILE_HEIGHT;
//                 g.drawRect(cx, cy, TILE_WIDTH, TILE_HEIGHT);
//                 String coord = (xx-_origX) + "/" + (yy-_origY);
//                 g.drawString(coord, cx+TILE_WIDTH/2, cy+TILE_HEIGHT/2);
//             }
//         }

        // iterate over our tiles, painting each of them
        for (AtlantiTile tile : _tiles) {
            tile.paint(g2, _origX, _origY);
        }

        // if we have a placing tile, draw that one as well
        if (_placingTile != null && _validPlacement) {
            // if the current position is valid, draw the placing tile
            _placingTile.paint(g2, _origX, _origY);

            // draw a green rectangle around the placing tile
            g.setColor(Color.blue);
            int sx = (_placingTile.x + _origX) * TILE_WIDTH;
            int sy = (_placingTile.y + _origY) * TILE_HEIGHT;
            g.drawRect(sx, sy, TILE_WIDTH-1, TILE_HEIGHT-1);
        }

        // if we have a recently placed tile, draw that one as well
        if (_placedTile != null) {
            // draw the tile
            _placedTile.paint(g2, _origX, _origY);

            // draw a white rectangle around the placed tile
            g.setColor(Color.white);
            int sx = (_placedTile.x + _origX) * TILE_WIDTH;
            int sy = (_placedTile.y + _origY) * TILE_HEIGHT;
            g.drawRect(sx, sy, TILE_WIDTH-1, TILE_HEIGHT-1);
        }

        // draw a white rectangle around the last placed
        if (_lastPlacedTile != null) {
            g.setColor(Color.white);
            int sx = (_lastPlacedTile.x + _origX) * TILE_WIDTH;
            int sy = (_lastPlacedTile.y + _origY) * TILE_HEIGHT;
            g.drawRect(sx, sy, TILE_WIDTH-1, TILE_HEIGHT-1);
        }

        // undo our translations
        g.translate(-_tx, -_ty);
    }

    /** Called by our adapter when the mouse moves. */
    protected void mouseMoved (MouseEvent evt)
    {
        // we always want to know about our last mouse coordinates
        _mouseX = evt.getX() - _tx;
        _mouseY = evt.getY() - _ty;

        if (_placingTile != null) {
            // if we have a tile to be placed, update its coordinates (it
            // will automatically be repainted)
            updatePlacingInfo(false);

        } else if (_placedTile != null && _placingPiecen) {
            // if we have a recently placed tile, we're doing piecen
            // placement; first convert the mouse coords into tile coords
            int mx = _mouseX - (_placedTile.x + _origX) * TILE_WIDTH;
            int my = _mouseY - (_placedTile.y + _origY) * TILE_HEIGHT;
            boolean changed = false;

            // now see if we're inside the placing tile
            if (mx >= 0 && mx < TILE_WIDTH && my >= 0 && my < TILE_HEIGHT) {
                int fidx = _placedTile.getFeatureIndex(mx, my);

                // if the feature is not already claimed, we can put a
                // piece there to indicate that it can be claimed
                if (_placedTile.claims[fidx] == 0) {
                    if (_placedTile.piecen == null ||
                        _placedTile.piecen.featureIndex != fidx) {
                        Piecen p = new Piecen(_newPiecenColor, 0, 0, fidx);
                        _placedTile.setPiecen(p, null);
                        changed = true;
                    }

                } else {
                    // we may need to clear out a piecen since we've moved
                    if (_placedTile.piecen != null) {
                        _placedTile.clearPiecen();
                        changed = true;
                    }
                }

            } else {
                // we may need to clear out a piecen since we've moved
                if (_placedTile.piecen != null) {
                    _placedTile.clearPiecen();
                    changed = true;
                }
            }

            if (changed) {
                repaintTile(_placedTile);
            }
        }
    }

    /** Called by our adapter when the mouse is clicked. */
    protected void mouseClicked (MouseEvent evt)
    {
        int modifiers = evt.getModifiers();

        // if this is a right button click, and we're in piecen placing
        // mode, generate a PLACE_NOTHING notification instead
        if (_placingPiecen && (modifiers & MouseEvent.BUTTON3_MASK) != 0) {
            // stop piecen placement
            _placingPiecen = false;
            // clear out any placed piecen because we're placing nothing
            if (_placedTile != null && _placedTile.piecen != null) {
                _placedTile.piecen = null;
                repaintTile(_placedTile);
            }
            // tell the controller we're done
            _ctrl.placeNothing();

        } else {
            // ignore non-button one presses other than cancel piecen placement
            if ((modifiers & MouseEvent.BUTTON1_MASK) == 0) {
                return;
            }
        }

        // if we have a placing tile and it's in a valid position, we want to dispatch an action
        // letting the controller know that the user placed it
        if (_placingTile != null && _validPlacement) {
            // move the placing tile to the placed tile
            _placedTile = _placingTile;
            _placingTile = null;

            // inherit claims on the placed tile
            TileUtil.inheritClaims(_tiles, _placedTile);

            // post the action
            _ctrl.tilePlaced(_placedTile);

            // move into placing piecen mode
            _placingPiecen = true;

            // recompute our dimensions (which will relayout or repaint)
            computeDimensions();
        }

        // if we're placing a piecen and the piecen is in a valid position, we
        // want to let the controller know that the user placed it
        if (_placingPiecen && _placedTile != null &&
            _placedTile.piecen != null) {
            _ctrl.piecenPlaced(_placedTile.piecen);
            // clear out placing piecen mode
            _placingPiecen = false;
        }
    }

    /**
     * Updates the coordinates and orientation of the placing tile based on the last known
     * coordinates of the mouse and causes it to be repainted.
     */
    protected void updatePlacingInfo (boolean force)
    {
        boolean updated = false;

        // convert mouse coordinates into tile coordinates and offset them by the origin
        int x = MathUtil.floorDiv(_mouseX, TILE_WIDTH) - _origX;
        int y = MathUtil.floorDiv(_mouseY, TILE_HEIGHT) - _origY;

        // if these are different than the values currently in the placing
        // tile, update the tile coordinates
        if (_placingTile.x != x || _placingTile.y != y || force) {
            // if we have a valid orientation presently, and we're moving,
            // we need to clear out the old orientation
            if (_validPlacement) {
                repaintTile(_placingTile);
            }

            // update the coordinates of the tile
            _placingTile.x = x;
            _placingTile.y = y;

            // make a note that we moved
            updated = true;

            // we also need to recompute the valid orientations for the tile in this new position
            _validOrients = TileUtil.computeValidOrients(_tiles, _placingTile);

            // if we've changed positions, clear out our valid placement flag
            _validPlacement = false;
        }

        // determine if we should change the orientation based on the
        // position of the mouse within the tile boundaries
        int rx = _mouseX % TILE_WIDTH;
        int ry = _mouseY % TILE_HEIGHT;
        int orient = coordToOrient(rx, ry);

        // scan for a legal orientation that is closest to our desired orientation
        for (int ii = 0; ii < 4; ii++) {
            int candOrient = (orient+ii)%4;
            if (_validOrients[candOrient]) {
                if (_placingTile.orientation != candOrient) {
                    _placingTile.orientation = candOrient;
                    // make a note that we moved
                    updated = true;
                }
                _validPlacement = true;
                break;
            }
        }

        // if we now have a valid orientation and something was changed,
        // we want to repaint at the new tile location
        if (_validPlacement && updated) {
            repaintTile(_placingTile);
        }
    }

    /**
     * Converts mouse coordinates which are relative to a particular tile, into an orientation
     * based on the position within that tile. A tile is divided up into four quadrants by lines
     * connecting its four corners. If the tile is in a quadrant closes to an edge, it is
     * converted to the orientation corresponding with that edge.
     *
     * @param rx the mouse coordinates modulo tile width.
     * @param ry the mouse coordinates modulo tile height.
     *
     * @return the orientation desired for the tile in which the mouse resides.
     */
    protected int coordToOrient (int rx, int ry)
    {
        if (rx > ry) {
            if (rx > (TILE_HEIGHT - ry)) {
                return EAST;
            } else {
                return NORTH;
            }
        } else {
            if (rx > (TILE_HEIGHT - ry)) {
                return SOUTH;
            } else {
                return WEST;
            }
        }
    }

    @Override
    public Dimension getPreferredSize ()
    {
        if (_tiles.size() == 0) {
            return new Dimension(100, 100);

        } else {
            return new Dimension(TILE_WIDTH * _width, TILE_HEIGHT * _height);
        }
    }

    /**
     * Determines how big we want to be based on where the tiles have been laid out. This will
     * cause the component to be re-layed out if the dimensions change or repainted if not.
     */
    protected void computeDimensions ()
    {
        int maxX = 0, maxY = 0;
        int minX = 0, minY = 0;

        // if we have a recently placed tile, start with that one
        if (_placedTile != null) {
            minX = maxX = _placedTile.x;
            minY = maxY = _placedTile.y;
        }

        // figure out what our boundaries are
        for (AtlantiTile tile : _tiles) {
            if (tile.x > maxX) {
                maxX = tile.x;
            } else if (tile.x < minX) {
                minX = tile.x;
            }
            if (tile.y > maxY) {
                maxY = tile.y;
            } else if (tile.y < minY) {
                minY = tile.y;
            }
        }

        // spread our bounds out by one
        minX -= 1; minY -= 1;
        maxX += 1; maxY += 1;

        // keep track of these to know if we've change dimensions
        int oldOrigX = _origX, oldOrigY = _origY;
        int oldWidth = _width, oldHeight = _height;

        // now we can compute our width and the origin offset
        _origX = -minX;
        _origY = -minY;
        _width = maxX - minX + 1;
        _height = maxY - minY + 1;

        if (_origX != oldOrigX || _origY != oldOrigY ||
            oldWidth != _width || oldHeight != _height) {
            // if the dimensions changed, we need to relayout
            revalidate();
        }

        // always repaint because revalidation doesn't always seem to result in a repaint
        repaint();
    }

    /** Test code. */
    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Board test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AtlantiBoardView board = new AtlantiBoardView(null);

        TestDSet set = new TestDSet();
        set.addTile(new AtlantiTile(CITY_TWO, true, WEST, 0, 0));
        set.addTile(new AtlantiTile(CITY_TWO, false, WEST, -1, 1));
        set.addTile(new AtlantiTile(CITY_ONE, false, SOUTH, -1, -1));
        AtlantiTile zero = new AtlantiTile(CURVED_ROAD, false, WEST, 0, 2);
        set.addTile(zero);
        AtlantiTile one = new AtlantiTile(TWO_CITY_TWO, false, NORTH, 0, 1);
        set.addTile(one);
        set.addTile(new AtlantiTile(CITY_THREE, false, WEST, 1, 1));
        set.addTile(new AtlantiTile(CITY_THREE_ROAD, false, EAST, 1, 2));
        set.addTile(new AtlantiTile(CITY_THREE, false, NORTH, -1, 0));
        AtlantiTile two = new AtlantiTile(CITY_ONE, false, EAST, -2, 0);
        set.addTile(two);
        board.tilesChanged(set);

        AtlantiTile placing = new AtlantiTile(CITY_TWO, false, NORTH, 0, 0);
        board.setTileToBePlaced(placing);

        // set a feature group to test propagation
        List<AtlantiTile> tiles = new ArrayList<AtlantiTile>();
        CollectionUtil.addAll(tiles, set.iterator());
        Collections.sort(tiles);

        zero.setPiecen(new Piecen(Piecen.GREEN, 0, 0, 2), tiles);
        one.setPiecen(new Piecen(Piecen.BLUE, 0, 0, 0), tiles);
        two.setPiecen(new Piecen(Piecen.RED, 0, 0, 1), tiles);

        log.info("Incomplete road: " + TileUtil.computeFeatureScore(tiles, zero, 2));

        log.info("Completed city: " + TileUtil.computeFeatureScore(tiles, two, 1));

        log.info("Incomplete city: " + TileUtil.computeFeatureScore(tiles, one, 2));

        frame.getContentPane().add(board, BorderLayout.CENTER);
        frame.pack();
        SwingUtil.centerWindow(frame);
        frame.setVisible(true);
    }

    protected static class TestDSet extends DSet<AtlantiTile>
    {
        public void addTile (AtlantiTile tile) {
            add(tile);
        }
    }

    /** The controller to which we dispatch commands. */
    protected AtlantiController _ctrl;

    /** A reference to our tile set. */
    protected ArrayList<AtlantiTile> _tiles = new ArrayList<AtlantiTile>();

    /** The tile currently being placed by the user. */
    protected AtlantiTile _placingTile;

    /** The last tile being placed by the user. */
    protected AtlantiTile _placedTile;

    /** The last tile placed on the board via {@link #addTile}. */
    protected AtlantiTile _lastPlacedTile;

    /** A flag indicating whether or not we're placing a piecen. */
    protected boolean _placingPiecen = false;

    /** Whether or not the current position and orientation of the placing tile is valid. */
    protected boolean _validPlacement = false;

    /** An array indicating which of the four directions are valid
     * placements based on the current position of the placing tile. */
    protected boolean[] _validOrients;

    /** The color to use when creating new piecens. */
    protected int _newPiecenColor = Piecen.BLUE;

    /** Our render offset in pixels. */
    protected int _tx, _ty;

    /** The offset in tile coordinates of the origin. */
    protected int _origX, _origY;

    /** The width and height of the board in tile coordinates. */
    protected int _width, _height;

    /** The last known position of the mouse. */
    protected int _mouseX, _mouseY;
}
