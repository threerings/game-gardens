//
// $Id: SkirmishObject.java,v 1.5 2002/07/16 19:43:15 mdb Exp $

package com.threerings.skirmish.data;

import com.threerings.parlor.game.GameObject;

/**
 * Maintains the board state of a skirmish game.
 */
public class SkirmishObject extends GameObject
{
    /** The field name of the <code>board</code> field. */
    public static final String BOARD = "board";

    /** The field name of the <code>vessels</code> field. */
    public static final String VESSELS = "vessels";

    /** The field name of the <code>hands</code> field. */
    public static final String HANDS = "hands";

    /** The field name of the <code>damage</code> field. */
    public static final String DAMAGE = "damage";

    /** The field name of the <code>actionCache</code> field. */
    public static final String ACTION_CACHE = "actionCache";

    /** The field name of the <code>nextTurn</code> field. */
    public static final String NEXT_TURN = "nextTurn";

    /** The field name of the <code>escapeCounter</code> field. */
    public static final String ESCAPE_COUNTER = "escapeCounter";

    /** The field name of the <code>attackerIndex</code> field. */
    public static final String ATTACKER_INDEX = "attackerIndex";

    /** The field name of the <code>handPos</code> field. */
    public static final String HAND_POS = "handPos";

    /** The board on which the game is played. */
    public SkirmishBoard board;

    /** The state of the vessels on the skirmish board. */
    public SkirmishVessel[] vessels;

    /** The hands being developed by each vessel for the upcoming turn. */
    public SkirmishHand[] hands;

    /** Damage levels for each vessel. */
    public int[] damage;

    /** Contains the number of each type of token currently available to
     * each user (in row major order). */
    public int[] actionCache;

    /** The time at which the next turn will be invoked. */
    public long nextTurn;

    /** The number of turns that have passed without the attacker hitting
     * another vessel. */
    public int escapeCounter;

    /** The player index of the attacker. */
    public int attackerIndex;

    /** Used to let the clients know which hand position is currently
     * being executed when the hands are being executed. */
    public int handPos = -1;

    /**
     * Generates a cloned copy of the vessels which can be modified and
     * broadcast as an update.
     */
    public SkirmishVessel[] cloneVessels ()
    {
        int vcount = vessels.length;
        SkirmishVessel[] nvessels = new SkirmishVessel[vcount];
        for (int ii = 0; ii < vcount; ii++) {
            nvessels[ii] = (SkirmishVessel)vessels[ii].clone();
        }
        return nvessels;
    }

    /**
     * Requests that the <code>board</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBoard (SkirmishBoard board)
    {
        this.board = board;
        requestAttributeChange(BOARD, board);
    }

    /**
     * Requests that the <code>vessels</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setVessels (SkirmishVessel[] vessels)
    {
        this.vessels = vessels;
        requestAttributeChange(VESSELS, vessels);
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>vessels</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setVesselsAt (SkirmishVessel value, int index)
    {
        this.vessels[index] = value;
        requestElementUpdate(VESSELS, value, index);
    }

    /**
     * Requests that the <code>hands</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setHands (SkirmishHand[] hands)
    {
        this.hands = hands;
        requestAttributeChange(HANDS, hands);
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>hands</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setHandsAt (SkirmishHand value, int index)
    {
        this.hands[index] = value;
        requestElementUpdate(HANDS, value, index);
    }

    /**
     * Requests that the <code>damage</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setDamage (int[] damage)
    {
        this.damage = damage;
        requestAttributeChange(DAMAGE, damage);
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>damage</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setDamageAt (int value, int index)
    {
        this.damage[index] = value;
        requestElementUpdate(DAMAGE, new Integer(value), index);
    }

    /**
     * Requests that the <code>actionCache</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setActionCache (int[] actionCache)
    {
        this.actionCache = actionCache;
        requestAttributeChange(ACTION_CACHE, actionCache);
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>actionCache</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setActionCacheAt (int value, int index)
    {
        this.actionCache[index] = value;
        requestElementUpdate(ACTION_CACHE, new Integer(value), index);
    }

    /**
     * Requests that the <code>nextTurn</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setNextTurn (long nextTurn)
    {
        this.nextTurn = nextTurn;
        requestAttributeChange(NEXT_TURN, new Long(nextTurn));
    }

    /**
     * Requests that the <code>escapeCounter</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setEscapeCounter (int escapeCounter)
    {
        this.escapeCounter = escapeCounter;
        requestAttributeChange(ESCAPE_COUNTER, new Integer(escapeCounter));
    }

    /**
     * Requests that the <code>attackerIndex</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setAttackerIndex (int attackerIndex)
    {
        this.attackerIndex = attackerIndex;
        requestAttributeChange(ATTACKER_INDEX, new Integer(attackerIndex));
    }

    /**
     * Requests that the <code>handPos</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setHandPos (int handPos)
    {
        this.handPos = handPos;
        requestAttributeChange(HAND_POS, new Integer(handPos));
    }
}
