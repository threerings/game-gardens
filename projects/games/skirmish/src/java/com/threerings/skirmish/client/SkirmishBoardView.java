//
// $Id: SkirmishBoardView.java,v 1.10 2002/07/27 00:45:43 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.JComponent;

import java.util.ArrayList;

import com.samskivert.util.IntervalManager;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.util.SafeInterval;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.skirmish.Log;
import com.threerings.skirmish.data.SkirmishBoard;
import com.threerings.skirmish.data.SkirmishCodes;
import com.threerings.skirmish.data.SkirmishObject;
import com.threerings.skirmish.data.SkirmishVessel;
import com.samskivert.util.ObserverList;

/**
 * Displays the Skirmish board and the vessels upon't.
 */
public class SkirmishBoardView extends JComponent
    implements PlaceView, SkirmishCodes, AttributeChangeListener
{
    /** The colors used to render different vessels. */
    public static final Color[] VESSEL_COLORS = {
        Color.pink, Color.yellow, Color.orange, Color.magenta };

    /** Implemented by entities that wish to know when the board view
     * updates its state to display the execution of a particular hand
     * position. */
    public static interface HandExecListener
    {
        public void updated (int handPos);
    }

    /**
     * Constructs a board view which will initialize itself and prepare to
     * display the Skirmish board.
     */
    public SkirmishBoardView (ParlorContext ctx)
    {
        _ctx = ctx;

        // create pop interval
        POP_INTERVAL = new SafeInterval(ctx.getClient()) {
            public void run () {
                popVessel();
            }
        };
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _skobj = (SkirmishObject)plobj;
        _skobj.addListener(this);

        // grab the current state of the vessels
        updateVessels(_skobj.vessels);

        // determine our index
        BodyObject self = (BodyObject)_ctx.getClient().getClientObject();
        _selfIndex = ListUtil.indexOf(_skobj.players, self.username);
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_skobj != null) {
            _skobj.removeListener(this);
            _skobj = null;
        }
    }

    public void addHandExecListener (HandExecListener listener)
    {
        if (!_helist.contains(listener)) {
            _helist.add(listener);
        }
    }

    public void removeHandExecListener (HandExecListener listener)
    {
        _helist.remove(listener);
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        // if the vessels were updated, update our display
        if (SkirmishObject.VESSELS.equals(event.getName())) {
            updateVessels((SkirmishVessel[])event.getValue());
        } else if (SkirmishObject.BOARD.equals(event.getName())) {
            repaint();
        }
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        // determine how many cells we can display on screen
        _cdims.width = getWidth() / CELL_SIZE;
        _cdims.height = getHeight() / CELL_SIZE;

        // center the view
        _vxoff = (getWidth() - _cdims.width * CELL_SIZE)/2;
        _vyoff = (getHeight() - _cdims.height * CELL_SIZE)/2;

        // if we have vessels, recenter the display
        if (_vessels != null) {
            centerOnVessels();
        }
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        g.setFont(COORD_FONT);
        FontMetrics fm = g.getFontMetrics();

        // fill the background with a color
        Color bgcolor = (_selfIndex == -1) ?
            Color.gray : VESSEL_COLORS[_selfIndex];
        g.setColor(bgcolor);
        g.fillRect(0, 0, getWidth(), getHeight());

        // nothing to do if we have no skobj or board yet
        if (_skobj == null || _skobj.board == null) {
            return;
        }

        g.translate(_vxoff, _vyoff);

        for (int yy = 0; yy < _cdims.height; yy++) {
            for (int xx = 0; xx < _cdims.width; xx++) {
                int xp = xx * CELL_SIZE, yp = yy * CELL_SIZE;
                byte feature = _skobj.board.getFeature(xx + _xoff, yy + _yoff);

                // draw coordinates for the first row and column
                String coords = null;
                if (xx == 0 && yy > 0) {
                    coords = String.valueOf(yy+_yoff);
                } else if (yy == 0 && xx > 0) {
                    coords = String.valueOf(xx+_xoff);
                }

                // fill the cell background with the color appropriate to
                // the feature
                g.setColor(CELL_COLORS[feature]);
                g.fillRect(xp, yp, CELL_SIZE, CELL_SIZE);

                // now draw the feature indicator
                g.setColor(Color.black);
                g.translate(xp, yp);
                switch (feature) {
                case SkirmishBoard.WHIRLPOOL_CELL:
                    g.drawOval(3, 3, CELL_SIZE-6, CELL_SIZE-6);
                    break;
                case SkirmishBoard.NORTH_WIND_CELL:
                    g.drawPolygon(NORTH_POLY);
                    break;
                case SkirmishBoard.EAST_WIND_CELL:
                    g.drawPolygon(EAST_POLY);
                    break;
                case SkirmishBoard.SOUTH_WIND_CELL:
                    g.drawPolygon(SOUTH_POLY);
                    break;
                case SkirmishBoard.WEST_WIND_CELL:
                    g.drawPolygon(WEST_POLY);
                    break;
                }

                if (coords != null) {
                    int swid = fm.stringWidth(coords);
                    g.drawString(coords, (CELL_SIZE-swid)/2,
                                 (CELL_SIZE-fm.getHeight())/2+fm.getAscent());
                }

                g.translate(-xp, -yp);
            }
        }

        // now render the vessels
        if (_vessels != null) {
            for (int vv = 0; vv < _vessels.length; vv++) {
                SkirmishVessel ves = _vessels[vv];
                int vx = (ves.column - _xoff) * CELL_SIZE;
                int vy = (ves.row - _yoff) * CELL_SIZE;
                g.setColor(VESSEL_COLORS[vv]);
                if (ves.orient == NORTH || ves.orient == SOUTH) {
                    g.fillOval(vx + CELL_SIZE/4, vy + 3,
                               CELL_SIZE/2, CELL_SIZE-6);
                    g.setColor(Color.black);
                    g.drawOval(vx + CELL_SIZE/4, vy + 3,
                               CELL_SIZE/2, CELL_SIZE-6);
                } else {
                    g.fillOval(vx + 3, vy + CELL_SIZE/4,
                               CELL_SIZE-6, CELL_SIZE/2);
                    g.setColor(Color.black);
                    g.drawOval(vx + 3, vy + CELL_SIZE/4,
                               CELL_SIZE-6, CELL_SIZE/2);
                }
                switch (ves.orient) {
                case NORTH:
                    g.drawLine(vx+CELL_SIZE/2, vy, vx + CELL_SIZE/2, vy + 6);
                    break;
                case EAST:
                    g.drawLine(vx + CELL_SIZE-6, vy + CELL_SIZE/2,
                               vx + CELL_SIZE, vy + CELL_SIZE/2);
                    break;
                case SOUTH:
                    g.drawLine(vx+CELL_SIZE/2, vy + CELL_SIZE-6,
                               vx + CELL_SIZE/2, vy + CELL_SIZE);
                    break;
                case WEST:
                    g.drawLine(vx, vy + CELL_SIZE/2, vx + 6, vy + CELL_SIZE/2);
                    break;
                }

                // draw the vessel number
                g.drawString(String.valueOf(vv), vx + CELL_SIZE/2-3,
                             vy + CELL_SIZE/2+5);

                // draw any shot being made by this vessel
                if (ves.shotOrient != -1) {
                    int sx = vx + CELL_SIZE/2, sy = vy + CELL_SIZE/2;
                    int dist = (ves.shotDist == -1) ?
                        CANNON_FIRE_DISTANCE : ves.shotDist;
                    int tx = sx, ty = sy;

                    switch (ves.shotOrient) {
                    case NORTH: ty -= dist * CELL_SIZE; break;
                    case EAST: tx += dist * CELL_SIZE; break;
                    case SOUTH: ty += dist * CELL_SIZE; break;
                    case WEST: tx -= dist * CELL_SIZE; break;
                    }
                    g.setColor(Color.red);
                    g.drawLine(sx, sy, tx, ty);

                    if (ves.shotDist == -1) {
                        g.setColor(Color.white);
                        g.drawOval(tx - CELL_SIZE/8, ty - CELL_SIZE/8,
                                   CELL_SIZE/4, CELL_SIZE/4);
                    } else {
                        g.drawOval(tx - CELL_SIZE/2, ty - CELL_SIZE/2,
                                   CELL_SIZE, CELL_SIZE);
                    }
                }
            }

            g.translate(-_vxoff, -_vyoff);
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        // this is the size of the ocean view when puzzling
        return new Dimension(339, 233);
    }

    /**
     * Queues up an update of the board display with the supplied vessel
     * states. Because vessel updates come in rapid succession, we queue
     * up the updates so that the humans have enough time to see and
     * process the displayed moves.
     */
    protected void updateVessels (SkirmishVessel[] vessels)
    {
        // if the vessels are blank or null, stop now
        if (vessels == null || vessels.length == 0) {
            return;
        }

        // if it's been long enough since we last updated our vessels, go
        // ahead and update them straight away
        long now = System.currentTimeMillis();
        long sinceLast = now - _lastVesselUpdate;
        if (sinceLast > VESSEL_UPDATE_DELAY) {
//             Log.info("Updating vessels directly " +
//                      StringUtil.toString(vessels) + ".");
            applyVessels(vessels, now, _skobj.handPos);

        } else {
            // otherwise queue up an update to be displayed after
            // sufficient time has elapsed
            VesselUpdate vup = new VesselUpdate();
            vup.vessels = vessels;
            vup.handPos = _skobj.handPos;
            _vupdates.add(vup);

            // if there's not already a pop interval queued, queue one up
            if (_popId == -1) {
                _popId = IntervalManager.register(
                    POP_INTERVAL, VESSEL_UPDATE_DELAY - sinceLast, null, true);
            }

//             Log.info("Queued vessel update " +
//                      StringUtil.toString(vessels) + ".");
        }
    }

    /**
     * Pops a vessel update from the queue and causes it to be rendered.
     */
    protected void popVessel ()
    {
        // pull an update from the queue and display it
        if (_vupdates.size() > 0) {
            VesselUpdate vup = _vupdates.remove(0);
            applyVessels(vup.vessels, System.currentTimeMillis(), vup.handPos);
//             Log.info("Popped and updated " +
//                      StringUtil.toString(_vessels) + ".");

        } else {
            // if we haven't received any vessel updates since the last
            // pop and the hand pos is back to -1, we must be done for
            // this turn; so we'll clear things out
            IntervalManager.remove(_popId);
            _popId = -1;

            // let our hand executors know that we're all done for now
            if (_skobj != null && _skobj.handPos == -1) {
                _lastHandPos = -1;
                _helist.apply(new ObserverList.ObserverOp() {
                    public boolean apply (Object observer) {
                        ((HandExecListener)observer).updated(_lastHandPos);
                        return true;
                    }
                });
            }
        }
    }

    /**
     * Applies a vessel update to the board, recentering the view,
     * notifying any hand execution listeners and generally doing the
     * business.
     */
    protected void applyVessels (
        SkirmishVessel[] vessels, long when, int handPos)
    {
        _vessels = vessels;
        _lastVesselUpdate = when;
        centerOnVessels();

        // if this is a new hand, let our listeners know that we're
        // rendering a new hand
        if (_lastHandPos != handPos) {
            _lastHandPos = handPos;
            _helist.apply(new ObserverList.ObserverOp() {
                public boolean apply (Object observer) {
                    ((HandExecListener)observer).updated(_lastHandPos);
                    return true;
                }
            });
        }
    }

    /**
     * Computes the board offsets necessary to ensure that the all vessels
     * are visible (if possible) and that the player's vessel is visible
     * in all circumstances.
     */
    protected void centerOnVessels ()
    {
        // if we have no game object, board or vessels, we'll have to bail
        if (_vessels == null || _skobj == null || _skobj.board == null) {
            return;
        }

        // determine the offset necessary to center the board on the two
        // vessels
        for (int ii = 0; ii < _vessels.length; ii++) {
            SkirmishVessel sv = _vessels[ii];
            if (ii == 0) {
                _crect.setBounds(sv.column, sv.row, 1, 1);
            } else {
                _crect.add(sv.column, sv.row);
            }
        }

        // we'll offset the display by this amount when rendering
        _xoff = _crect.x - (_cdims.width - _crect.width)/2;
        _xoff = Math.min(Math.max(_xoff, 0), _skobj.board.width-_cdims.width);
        _yoff = _crect.y - (_cdims.height - _crect.height)/2;
        _yoff = Math.min(Math.max(_yoff, 0), _skobj.board.height-_cdims.height);

        // ensure that our vessel is always visible (or some vessel if
        // we're not a player)
        int vidx = (_selfIndex == -1) ? 0 : _selfIndex;
        SkirmishVessel fves = _vessels[vidx];
        _xoff = Math.min(fves.column, _xoff);
        _xoff = Math.max(fves.column - _cdims.width + 1, _xoff);
        _yoff = Math.min(fves.row, _yoff);
        _yoff = Math.max(fves.row - _cdims.height + 1, _yoff);

//         Log.info("Centered on [crect=" + StringUtil.toString(_crect) +
//                  ", xoff=" + _xoff + ", yoff=" + _yoff +
//                  ", vessels=" + StringUtil.toString(_vessels) + "].");
//         System.out.println(_skobj.board);

        repaint();
    }

    /** Provides access to client services. */
    protected ParlorContext _ctx;

    /** A reference to our game object. */
    protected SkirmishObject _skobj;

    /** Our index into the players array. */
    protected int _selfIndex;

    /** The current vessel configuration being displayed on the board. */
    protected SkirmishVessel[] _vessels;

    /** The number of cells we can display on screen. */
    protected Dimension _cdims = new Dimension();

    /** Used when centering the display. */
    protected Rectangle _crect = new Rectangle();

    /** Used to center the display. */
    protected int _vxoff, _vyoff;

    /** Used to center the display. */
    protected int _xoff, _yoff;

    /** A timestamp of when we last updated our vessels. */
    protected long _lastVesselUpdate;

    /** A queue of vessel updates. */
    protected ArrayList<VesselUpdate> _vupdates = new ArrayList<VesselUpdate>();

    /** Used when popping a vessel update off the queue for rendering. */
    protected int _popId = -1;

    /** Used when popping a vessel update off the queue for rendering. */
    protected SafeInterval POP_INTERVAL;

    /** A list of entities to be notified when we render the actions
     * associated with the execution of a particular hand position. */
    protected ObserverList _helist =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The hand position last time we received a vessel update. */
    protected int _lastHandPos = -1;

    /** Used to queue up vessel updates for delayed display. */
    protected static class VesselUpdate
    {
        /** The state of the vessels at this update. */
        public SkirmishVessel[] vessels;

        /** The hand position being executed. */
        public int handPos;
    }

    /** The number of pixels on a side used to render each cell. */
    protected static final int CELL_SIZE = 32;

    /** Cell colors associated with each cell feature type. */
    protected static final Color[] CELL_COLORS = {
        Color.blue, // EMPTY
        Color.green, // WHIRLPOOL
        Color.white, // NORTH_WIND
        Color.white, // EAST_WIND
        Color.white, // SOUTH_WIND
        Color.white, // WEST_WIND
    };

    /** Polygon used to render the wind in the north direction. */
    protected static final Polygon NORTH_POLY =
        new Polygon(new int[] { CELL_SIZE/2, CELL_SIZE-3,           3 },
                    new int[] {           3, CELL_SIZE-3, CELL_SIZE-3 }, 3);

    /** Polygon used to render the wind in the east direction. */
    protected static final Polygon EAST_POLY =
        new Polygon(new int[] { 3, CELL_SIZE-3,           3 },
                    new int[] { 3, CELL_SIZE/2, CELL_SIZE-3 }, 3);

    /** Polygon used to render the wind in the south direction. */
    protected static final Polygon SOUTH_POLY =
        new Polygon(new int[] { 3, CELL_SIZE-3, CELL_SIZE/2 },
                    new int[] { 3,           3, CELL_SIZE-3 }, 3);

    /** Polygon used to render the wind in the west direction. */
    protected static final Polygon WEST_POLY =
        new Polygon(new int[] { CELL_SIZE-3, CELL_SIZE-3,           3 },
                    new int[] {           3, CELL_SIZE-3, CELL_SIZE/2 }, 3);

    /** Let the poor humans see the vessels for a couple of seconds before
     * moving on to the next move. */
    protected static final long VESSEL_UPDATE_DELAY = 750l;

    protected static Font COORD_FONT = new Font("Helvetica", Font.PLAIN, 10);
}
