//
// $Id: SkirmishObject.dobj,v 1.6 2002/07/16 19:43:15 mdb Exp $

package com.threerings.skirmish.data;

import com.threerings.parlor.game.GameObject;

/**
 * Maintains the board state of a skirmish game.
 */
public class SkirmishObject extends GameObject
{
    // AUTO-GENERATED: FIELDS START
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
    // AUTO-GENERATED: FIELDS END

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

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>board</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBoard (SkirmishBoard value)
    {
        SkirmishBoard ovalue = this.board;
        requestAttributeChange(
            BOARD, value, ovalue);
        this.board = value;
    }

    /**
     * Requests that the <code>vessels</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setVessels (SkirmishVessel[] value)
    {
        SkirmishVessel[] ovalue = this.vessels;
        requestAttributeChange(
            VESSELS, value, ovalue);
        this.vessels = (value == null) ? null : (SkirmishVessel[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>vessels</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setVesselsAt (SkirmishVessel value, int index)
    {
        SkirmishVessel ovalue = this.vessels[index];
        requestElementUpdate(
            VESSELS, index, value, ovalue);
        this.vessels[index] = value;
    }

    /**
     * Requests that the <code>hands</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setHands (SkirmishHand[] value)
    {
        SkirmishHand[] ovalue = this.hands;
        requestAttributeChange(
            HANDS, value, ovalue);
        this.hands = (value == null) ? null : (SkirmishHand[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>hands</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setHandsAt (SkirmishHand value, int index)
    {
        SkirmishHand ovalue = this.hands[index];
        requestElementUpdate(
            HANDS, index, value, ovalue);
        this.hands[index] = value;
    }

    /**
     * Requests that the <code>damage</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setDamage (int[] value)
    {
        int[] ovalue = this.damage;
        requestAttributeChange(
            DAMAGE, value, ovalue);
        this.damage = (value == null) ? null : (int[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>damage</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setDamageAt (int value, int index)
    {
        int ovalue = this.damage[index];
        requestElementUpdate(
            DAMAGE, index, new Integer(value), new Integer(ovalue));
        this.damage[index] = value;
    }

    /**
     * Requests that the <code>actionCache</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setActionCache (int[] value)
    {
        int[] ovalue = this.actionCache;
        requestAttributeChange(
            ACTION_CACHE, value, ovalue);
        this.actionCache = (value == null) ? null : (int[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>actionCache</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setActionCacheAt (int value, int index)
    {
        int ovalue = this.actionCache[index];
        requestElementUpdate(
            ACTION_CACHE, index, new Integer(value), new Integer(ovalue));
        this.actionCache[index] = value;
    }

    /**
     * Requests that the <code>nextTurn</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setNextTurn (long value)
    {
        long ovalue = this.nextTurn;
        requestAttributeChange(
            NEXT_TURN, new Long(value), new Long(ovalue));
        this.nextTurn = value;
    }

    /**
     * Requests that the <code>escapeCounter</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setEscapeCounter (int value)
    {
        int ovalue = this.escapeCounter;
        requestAttributeChange(
            ESCAPE_COUNTER, new Integer(value), new Integer(ovalue));
        this.escapeCounter = value;
    }

    /**
     * Requests that the <code>attackerIndex</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setAttackerIndex (int value)
    {
        int ovalue = this.attackerIndex;
        requestAttributeChange(
            ATTACKER_INDEX, new Integer(value), new Integer(ovalue));
        this.attackerIndex = value;
    }

    /**
     * Requests that the <code>handPos</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setHandPos (int value)
    {
        int ovalue = this.handPos;
        requestAttributeChange(
            HAND_POS, new Integer(value), new Integer(ovalue));
        this.handPos = value;
    }
    // AUTO-GENERATED: METHODS END
}
