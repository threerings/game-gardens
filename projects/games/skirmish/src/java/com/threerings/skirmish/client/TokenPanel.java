//
// $Id: TokenPanel.java,v 1.19 2002/07/27 00:46:59 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.VGroupLayout;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;
import com.threerings.util.RandomUtil;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.skirmish.Log;
import com.threerings.skirmish.data.SkirmishAction;
import com.threerings.skirmish.data.SkirmishCodes;
import com.threerings.skirmish.data.SkirmishHand;
import com.threerings.skirmish.data.SkirmishObject;

/**
 * Displays the tokens available to the player for selection.
 */
public class TokenPanel extends JComponent
    implements MouseMotionListener, MouseListener, PlaceView, SkirmishCodes,
               AttributeChangeListener, ElementUpdateListener
{
    public static final int SAILING_TYPE = 0;
    public static final int GUNNERY_TYPE = 1;

    public static final int WHEEL_LEFT = 0;
    public static final int WHEEL_CENTER = 1;
    public static final int WHEEL_RIGHT = 2;

    /**
     * Creates a token panel and prepares it for operation.
     */
    public TokenPanel (ParlorContext ctx, int tokenSpeed)
    {
        _ctx = ctx;
        _tokenSpeed = tokenSpeed;

        addMouseListener(this);
        addMouseMotionListener(this);

        // start with a random wheel state
        spinWheel();
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _skobj = (SkirmishObject)plobj;
        _skobj.addListener(this);

        // find out what our player index is
        BodyObject self = (BodyObject)_ctx.getClient().getClientObject();
        _selfIndex = ListUtil.indexOf(_skobj.players, self.username);

        // grab our initial hand
        grabHand();
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_skobj != null) {
            _skobj.removeListener(this);
            _skobj = null;
        }
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (SkirmishObject.HANDS.equals(event.getName())) {
            // update the hand display
            grabHand();
        }
    }

    // documentation inherited from interface
    public void elementUpdated (ElementUpdatedEvent event)
    {
        if (SkirmishObject.HANDS.equals(event.getName())) {
            // update the hand display
            grabHand();
        }
    }

    /**
     * Sets the wheel state to either left, forward or right which
     * controls which token type toward which sailing output is applied.
     */
    public void setWheelState (int wheelState)
    {
        _wheelState = wheelState;
    }

    /**
     * Applies the specified amount of puzzling output toward the
     * generation of the appropriate piece type.
     */
    public void applyPuzzlingOutput (int puzType, int amount)
    {
        // ignore this if we're not a player
        if (_selfIndex == -1) {
            return;
        }

        // adjust the amount according to our damage
        double dmgpct = 1.0 -
            (double)Math.min(10, _skobj.damage[_selfIndex]) / 10;
        int oamount = amount;
        amount = (int)Math.round(dmgpct * amount);

//         Log.info("Scaled amount [dmg=" + _skobj.damage[_selfIndex] +
//                  ", pct=" + dmgpct + ", oamount=" + oamount +
//                  ", amount=" + amount + "].");

        // apply the output toward the appropriate action type
        boolean created = false;
        switch (puzType) {
        case SAILING_TYPE:
            // if this is sailing output, we need to apply the output
            // based on the wheel position
            switch (_wheelState) {
            case WHEEL_LEFT:
                created = accumOutput(SkirmishAction.TURN_LEFT_ACTION, amount);
                break;
            case WHEEL_CENTER:
                created = accumOutput(SkirmishAction.FORWARD_ACTION, amount);
                break;
            case WHEEL_RIGHT:
                created = accumOutput(SkirmishAction.TURN_RIGHT_ACTION, amount);
                break;
            default:
                Log.warning("Eh? Wheel in unknown state " +
                            "[state=" + _wheelState +
                            ", amount=" + amount + "].");
                break;
            }
            break;

        case GUNNERY_TYPE:
            accumOutput(SkirmishAction.FIRE_ACTION, amount);
            break;

        default:
            Log.warning("Requested to apply output to unknown puzzle type " +
                        "[type=" + puzType + ", amount=" + amount + "].");
            break;
        }

        // if we created a sailing token, spin the wheel
        if (created) {
            spinWheel();
        }
    }

    /**
     * Applies puzzling output toward the creation of a specific type of
     * action token.
     *
     * @return true if we created a token, false if not.
     */
    protected boolean accumOutput (byte actionType, int amount)
    {
        int aidx = SkirmishAction.toIndex(actionType);
        _actionAccum[aidx] += amount;
        boolean created = false;

        // if we have enough output to create a token, do so
        while (_actionAccum[aidx] >= newTokenAmount(aidx)) {
            // update our action cache field appropriately
            int cidx = _selfIndex*4 + aidx;
            _skobj.setActionCacheAt(_skobj.actionCache[cidx] + 1, cidx);
            created = true;

            // and reduce our accumulator by the appropriate amount
            _actionAccum[aidx] -= newTokenAmount(aidx);
        }

        // and repaint to let the user know what's up
        repaint();
        return created;
    }

    /**
     * Chooses a random wheel state, weighted appropriately.
     */
    protected void spinWheel ()
    {
        _wheelState = WHEEL_STATES[RandomUtil.getInt(WHEEL_STATES.length)];
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        // create the rectangles that identify the positions of the tokens
        // in our hand if we haven't already
        int xpos = 0, ypos = 0;
        _handRects = new Rectangle[TokenUtil.TOKENS_ACROSS];
        _fireRects = new Rectangle[TokenUtil.TOKENS_ACROSS];
        for (int ii = 0; ii < _handRects.length; ii++) {
            int size = TokenUtil.TOKEN_SIZE;
            _handRects[ii] = new Rectangle(xpos, ypos, size, size);
            xpos += (size + TokenUtil.TOKEN_GAP);
            size = TokenUtil.SMALL_TOKEN_SIZE;
            _fireRects[ii] = new Rectangle(xpos, ypos, size, size);
            xpos += (size + TokenUtil.TOKEN_GAP);
        }

        // create the rectangles that identify the positions of our action
        // cache tokens
        int tokwid = getWidth()/4;
        xpos = 0;
        ypos += (TokenUtil.TOKEN_SIZE + TokenUtil.TOKEN_GAP);
        _cacheRects = new Rectangle[SkirmishAction.CACHED_COUNT];
        for (int ii = 0; ii < SkirmishAction.CACHED_COUNT; ii++) {
            _cacheRects[ii] = new Rectangle(
                xpos, ypos, TokenUtil.TOKEN_SIZE, TokenUtil.TOKEN_SIZE);
            xpos += tokwid;
        }
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        FontMetrics fm = getFontMetrics(_font);
        g.setFont(_font);

        // draw our hand of action tokens
        if (_hand != null) {
            int hcount = _hand.actions.length;
            for (int ii = 0; ii < hcount; ii++) {
                TokenUtil.renderAction(g, _hand.actions[ii].code,
                                       _handRects[ii].x, _handRects[ii].y,
                                       false);
                TokenUtil.renderAction(g, _hand.firings[ii].code,
                                       _fireRects[ii].x, _fireRects[ii].y,
                                       true);
            }
        }

        // and render our action cache
        for (int ii = 0; ii < SkirmishAction.CACHED_COUNT; ii++) {
            byte actionType = SkirmishAction.fromIndex(ii);
            Rectangle rect = _cacheRects[ii];

            // draw the action token
            TokenUtil.renderAction(g, actionType, rect.x, rect.y,
                                   actionType == SkirmishAction.FIRE_ACTION);

            // and draw the progress thus far toward another token
            g.setColor(Color.blue);
            int py = rect.y + TokenUtil.TOKEN_SIZE + TokenUtil.TOKEN_GAP;
            int pwid = (TokenUtil.TOKEN_SIZE * _actionAccum[ii]) /
                newTokenAmount(ii);
            g.fillRect(rect.x, py, pwid, PROGRESS_HEIGHT);
            g.setColor(Color.black);
            g.drawRect(rect.x, py, TokenUtil.TOKEN_SIZE-1, PROGRESS_HEIGHT-1);

            // and the number of such tokens remaining
            int acount = availableTokens(actionType);
            String tstr = String.valueOf(acount);
            int sx = rect.x + TokenUtil.TOKEN_SIZE + TokenUtil.TOKEN_GAP,
                sy = rect.y + TokenUtil.TOKEN_SIZE + fm.getAscent();
            g.drawString(tstr, sx, sy);
        }

        byte code = -1;
        if (_handDrag != null) {
            code = _handDrag.code;
        } else if (_cacheDrag != null) {
            code = _cacheDrag.code;
        }
        if (code != -1) {
            boolean small = (code == SkirmishAction.FIRE_ACTION);
            int size = (small ? TokenUtil.TOKEN_SIZE :
                        TokenUtil.SMALL_TOKEN_SIZE)/2;
            TokenUtil.renderAction(
                g, code, _dragPos.x - size, _dragPos.y - size, small);
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        FontMetrics fm = getFontMetrics(_font);
        int tdim = TokenUtil.TOKEN_SIZE + TokenUtil.TOKEN_GAP;
        return new Dimension(
            TokenUtil.TOKENS_ACROSS * tdim - TokenUtil.TOKEN_GAP,
            2 * tdim + fm.getHeight());
    }

    // documentation inherited from interface
    public void mousePressed (MouseEvent event)
    {
        // no dragging if we've not yet got a hand or we're not a playah
        if (_hand == null || _selfIndex == -1) {
            return;
        }
        int mx = event.getX(), my = event.getY();

        // a drag can initiate with a regular token already in the hand...
        for (int ii = 0; ii < _hand.actions.length; ii++) {
            if (_handRects[ii].contains(mx, my)) {
                if (_hand.actions[ii].code != SkirmishAction.NOOP_ACTION) {
                    _handDrag = _hand.actions[ii];
                    _handDragIndex = ii;
                    _dragPos.setLocation(mx, my);
                    repaint();
                }
                return;
            }
        }

        // ...or with a firing token...
        for (int ii = 0; ii < _hand.actions.length; ii++) {
            if (_fireRects[ii].contains(mx, my)) {
                if (_hand.firings[ii].code != SkirmishAction.NOOP_ACTION) {
                    _handDrag = _hand.firings[ii];
                    _fireDragIndex = ii;
                    _dragPos.setLocation(mx, my);
                    repaint();
                }
                return;
            }
        }

        // ...or with a token from the cache
        for (int ii = 0; ii < _cacheRects.length; ii++) {
            if (_cacheRects[ii].contains(mx, my)) {
                // make sure there are tokens available of this type
                byte actionType = SkirmishAction.fromIndex(ii);
                if (availableTokens(actionType) == 0) {
                    return;
                }

                // if this was a double click, simply append this
                // token to the end of the current hand
                if (event.getClickCount() == 2) {
                    // look for the first non-NOOP starting from the end
                    // of the hand and append it after that
                    int insertPos = 0;
                    for (int hh = _hand.actions.length-1; hh >= 0; hh--) {
                        if (_hand.actions[hh].code !=
                            SkirmishAction.NOOP_ACTION) {
                            insertPos = hh+1;
                            break;
                        }
                    }
                    if (insertPos < _hand.actions.length &&
                        actionType != SkirmishAction.FIRE_ACTION) {
                        _hand.actions[insertPos] =
                            new SkirmishAction(actionType);
                        sendHand();
                    }

                } else {
                    // otherwise prepare for this token to be dragged
                    _cacheDrag = new SkirmishAction(actionType);
                    _dragPos.setLocation(mx, my);
                }
                repaint();
                return;
            }
        }
    }

    // documentation inherited from interface
    public void mouseReleased (MouseEvent event)
    {
        int mx = event.getX(), my = event.getY();

        // they may have been dragging a token from their hand...
        if (_handDrag != null) {
            if (_handDrag.code == SkirmishAction.FIRE_ACTION) {
                // they might be dragging a piece out of the hand or
                // rearranging the hand
                int slot = getFireSlot(mx, my);
                if (slot == -1) {
                    // they dropped it on nothing; so we remove it from
                    // their hand
                    _hand.firings[_fireDragIndex] = SkirmishAction.NOOP;
                    sendHand();

                } else {
                    // swap this slot with the dragged slot
                    _hand.firings[_fireDragIndex] = _hand.firings[slot];
                    _hand.firings[slot] = _handDrag;
                    sendHand();
                }

            } else {
                // they might be dragging a piece out of the hand or
                // rearranging the hand
                int slot = getHandSlot(mx, my);
                if (slot == -1) {
                    // they dropped it on nothing; so we remove it from
                    // their hand
                    _hand.actions[_handDragIndex] = SkirmishAction.NOOP;
                    sendHand();

                } else {
                    // swap this slot with the dragged slot
                    _hand.actions[_handDragIndex] = _hand.actions[slot];
                    _hand.actions[slot] = _handDrag;
                    sendHand();
                }
            }

            _handDrag = null;
            _handDragIndex = -1;
            _fireDragIndex = -1;
            repaint();
        }

        // ...or one from the cache
        if (_cacheDrag != null) {
            if (_cacheDrag.code == SkirmishAction.FIRE_ACTION) {
                int slot = getFireSlot(mx, my);
                if (slot == -1) {
                    // they dropped it on nothing; so we do nothing
                } else {
                    // overwrite this slot with the dragged piece and the
                    // other will automatically be "returned" to the cache
                    _hand.firings[slot] = _cacheDrag;
                    sendHand();
                }

            } else {
                int slot = getHandSlot(mx, my);
                if (slot == -1) {
                    // they dropped it on nothing; so we do nothing
                } else {
                    // overwrite this slot with the dragged piece and the
                    // other will automatically be "returned" to the cache
                    _hand.actions[slot] = _cacheDrag;
                    sendHand();
                }
            }

            _cacheDrag = null;
            repaint();
        }
    }

    // documentation inherited from interface
    public void mouseClicked (MouseEvent event)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void mouseExited (MouseEvent event)
    {
        // nothing doing
    }
 
    // documentation inherited from interface
    public void mouseEntered (MouseEvent event)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void mouseDragged (MouseEvent event)
    {
        if (_handDrag != null || _cacheDrag != null) {
            _dragPos.setLocation(event.getX(), event.getY());
            repaint();
        }
    }

    // documentation inherited from interface
    public void mouseMoved (MouseEvent event)
    {
        // nothing doing
    }

    /** Grabs our hand from the game object and repaints. */
    protected void grabHand ()
    {
        if (_selfIndex != -1 && _skobj.hands != null) {
            _hand = _skobj.hands[_selfIndex];
//             Log.info("Updating hand " +
//                      StringUtil.toString(_hand.actions) + ".");
            repaint();
        }
    }

    /**
     * Returns the number of action tokens of the specified type available
     * in the cache (those already in the user's current hand are not
     * considered available).
     */
    protected int availableTokens (byte actionType)
    {
        // don't freak out if things haven't yet been set up or if we're
        // not a player
        if (_skobj.actionCache == null || _skobj.actionCache.length == 0 ||
            _selfIndex == -1) {
            return 0;
        }

        int aidx = SkirmishAction.toIndex(actionType);
        return (_skobj.actionCache[_selfIndex*4+aidx] -
                _hand.actionCount(actionType));
    }

    /**
     * Returns the index of the hand slot that contains the specified
     * mouse coordinates or -1 if no coordinates contain that slot.
     */
    protected int getHandSlot (int x, int y)
    {
        // not to freak out if we've not yet received our hand
        if (_hand == null) {
            return -1;
        }
        for (int ii = 0; ii < _hand.actions.length; ii++) {
            if (_handRects[ii].contains(x, y)) {
                return ii;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the hand slot that contains the specified
     * mouse coordinates or -1 if no coordinates contain that slot.
     */
    protected int getFireSlot (int x, int y)
    {
        // not to freak out if we've not yet received our hand
        if (_hand == null) {
            return -1;
        }
        for (int ii = 0; ii < _hand.firings.length; ii++) {
            if (_fireRects[ii].contains(x, y)) {
                return ii;
            }
        }
        return -1;
    }

    /**
     * Issues a request to send our newly updated hand to the server.
     */
    protected void sendHand ()
    {
        MessageEvent mevt = new MessageEvent(
            _skobj.getOid(), SET_HAND_REQUEST, new Object[] { _hand });
        _ctx.getClient().getDObjectManager().postEvent(mevt);
    }

    /**
     * Returns the amount required to create a new token with the
     * specified type index.
     */
    protected int newTokenAmount (int index)
    {
        float amount = NEW_TOKEN_AMOUNT[index];
        amount *= _tokenSpeed;
        amount /= 5;
        return (int)amount;
    }

    /** Provides access to general client services. */
    protected ParlorContext _ctx;

    /** The token speed configuration. */
    protected int _tokenSpeed;

    /** A reference to the game object. */
    protected SkirmishObject _skobj;

    /** Our index in the players array. */
    protected int _selfIndex;

    /** Our hand of actions. */
    protected SkirmishHand _hand;

    /** Used to render our hand. */
    protected Rectangle[] _handRects;

    /** Used to render our hand. */
    protected Rectangle[] _fireRects;

    /** Used to track how much puzzling output has been applied toward the
     * creation of each type of action token. */
    protected int[] _actionAccum = new int[4];

    /** Used to track mouse clicks over our action cache tokens. */
    protected Rectangle[] _cacheRects;

    /** The action currently being drug from the hand (or null). */
    protected SkirmishAction _handDrag = null;

    /** The index of the hand token being dragged. */
    protected int _handDragIndex = -1;

    /** The index of the hand token being dragged. */
    protected int _fireDragIndex = -1;

    /** The action currently being drug from the cache (or null). */
    protected SkirmishAction _cacheDrag = null;

    /** The last known mouse coordinates for a dragged component. */
    protected Point _dragPos = new Point();

    /** The orientation of the wheel: left, center or right. */
    protected int _wheelState = WHEEL_CENTER;

    /** Used to render the player name and damage level. */
    protected Font _font = new Font("Helvetica", Font.PLAIN, 12);

    /** The number of puzzling "units" needed to create a new token. */
    protected static final int[] NEW_TOKEN_AMOUNT = { 100, 100, 100, 200 };

    /** The width of the "new token" progress bar. */
    protected static final int PROGRESS_WIDTH = 70;

    /** The height of the "new token" progress bar. */
    protected static final int PROGRESS_HEIGHT = 10;

    /** Our randomly selectable wheel states. */
    protected static final int[] WHEEL_STATES = {
        WHEEL_CENTER, WHEEL_LEFT, WHEEL_CENTER, WHEEL_RIGHT };
}
