//
// $Id: PlayerView.java,v 1.8 2002/07/27 00:45:43 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import javax.swing.JPanel;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.skirmish.Log;
import com.threerings.skirmish.data.SkirmishAction;
import com.threerings.skirmish.data.SkirmishHand;
import com.threerings.skirmish.data.SkirmishObject;

/**
 * Displays info for a particular player including their damage level,
 * their developing hand and their hand as it is being executed.
 */
public class PlayerView extends JPanel
    implements PlaceView, AttributeChangeListener, ElementUpdateListener,
               SkirmishBoardView.HandExecListener
{
    public PlayerView (int playerIdx, SkirmishBoardView board)
    {
        _playerIdx = playerIdx;

        // fill in the background with our color
        setBackground(_playerIdx == -1 ? Color.gray :
                      SkirmishBoardView.VESSEL_COLORS[_playerIdx]);
        setOpaque(true);

        // register as a hand exec listener
        board.addHandExecListener(this);
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _skobj = (SkirmishObject)plobj;
        _skobj.addListener(this);

        // grab our initial hand
        readHand();
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
        String fname = event.getName();
        if (fname.equals(SkirmishObject.DAMAGE)) {
            repaint();
        } else if (fname.equals(SkirmishObject.ATTACKER_INDEX)) {
            repaint();
        }
    }

    // documentation inherited from interface
    public void elementUpdated (ElementUpdatedEvent event)
    {
        // we only care about updates for our player
        if (event.getIndex() != _playerIdx) {
            return;
        }

        String fname = event.getName();
        if (fname.equals(SkirmishObject.HANDS)) {
            readHand();
        } else if (fname.equals(SkirmishObject.DAMAGE)) {
            repaint();
        }
    }

    // documentation inherited from interface
    public void updated (int handPos)
    {
        _handPos = handPos;
        // if handPos == -1, update our hand with the latest hand
        if (handPos == -1) {
            _hand = _skobj.hands[_playerIdx];
        }
        repaint();
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // construct our info string
        StringBuffer info = new StringBuffer();
        info.append(_skobj.players[_playerIdx]);
        if (_playerIdx == _skobj.attackerIndex) {
            info.append(" [Attacker]");
        }
        if (_skobj.damage != null) {
            info.append(" (").append(_skobj.damage[_playerIdx]).append(")");
        }

        // render our info string
        FontMetrics fm = getFontMetrics(_font);
        g.setColor(Color.black);
        g.setFont(_font);
        g.drawString(info.toString(), 0, fm.getAscent());

        // bail now if we've got no hand
        if (_hand == null) {
            return;
        }

        // if we're in the middle of executing a hand, render the pieces
        // that have been executed normally; render the rest "flipped
        // over"
        int x = 0, y = fm.getHeight();
        for (int ii = 0; ii < _hand.actions.length; ii++) {
            renderAction(g, _hand.actions[ii].code, ii, x, y, false);
            x += (TokenUtil.TOKEN_SIZE + TokenUtil.TOKEN_GAP);
            renderAction(g, _hand.firings[ii].code, ii, x, y, true);
            x += (TokenUtil.SMALL_TOKEN_SIZE + TokenUtil.TOKEN_GAP);
        }
    }

    /**
     * Used when rendering the hand.
     */
    protected void renderAction (
        Graphics g, byte code, int handPos, int x, int y, boolean small)
    {
        if (code != SkirmishAction.NOOP_ACTION) {
            if (handPos <= _handPos) {
                TokenUtil.renderAction(g, code, x, y, small);
            } else {
                TokenUtil.renderBack(g, x, y, small);
            }
        }
    }

    public Dimension getPreferredSize ()
    {
        int tdim = TokenUtil.TOKEN_SIZE + TokenUtil.TOKEN_GAP;
        FontMetrics fm = getFontMetrics(_font);
        return new Dimension(
            TokenUtil.TOKENS_ACROSS * tdim - TokenUtil.TOKEN_GAP,
            fm.getHeight() + TokenUtil.TOKEN_SIZE);
    }

    protected void readHand ()
    {
        if (_skobj.hands == null || _skobj.hands.length == 0) {
            return;
        }

        SkirmishHand hand = _skobj.hands[_playerIdx];
        // if we don't yet have a hand; or this hand is for the same turn
        // as we're currently displaying, or if it's for the next turn but
        // we're no longer animating the previous hand, we take it
        if (_hand == null || _hand.turnCounter == hand.turnCounter ||
            _handPos == -1) {
            _hand = hand;
        }
        repaint();
    }

    /** The index of the player whose info we're displaying. */
    protected int _playerIdx;

    /** The game object. */
    protected SkirmishObject _skobj;

    /** The hand of the player whose info we're displaying. */
    protected SkirmishHand _hand;

    /** The position being currently executed in the hand we're displaying
     * or -1 if we're not executing. */
    protected int _handPos = -1;

    /** Used to render the player name and damage level. */
    protected Font _font = new Font("Helvetica", Font.PLAIN, 12);
}
