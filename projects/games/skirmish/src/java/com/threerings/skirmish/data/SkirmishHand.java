//
// $Id: SkirmishHand.java,v 1.6 2002/07/26 21:53:22 mdb Exp $

package com.threerings.skirmish.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Represents a "hand" of actions for a particular vessel. This "hand"
 * will be executed on the vessel with which it is associated when the
 * turn timer expires.
 */
public class SkirmishHand extends SimpleStreamableObject
{
    /** A monotonically increasing integer used to prevent a client from
     * accidentally submitting an updated hand after the server has
     * processed the turn for which that hand was valid. */
    public int turnCounter;

    /** The actions to be taken on each "tick". */
    public SkirmishAction[] actions;

    /** Optional cannon firing actions for each "tick". */
    public SkirmishAction[] firings;

    /**
     * Constructs a skirmish hand with the specified maximum number of
     * actions.
     */
    public SkirmishHand (int maxActions)
    {
        actions = new SkirmishAction[maxActions];
        firings = new SkirmishAction[maxActions];

        // start with a clean hand
        reset(0);
    }

    /**
     * Used when unserializing.
     */
    public SkirmishHand ()
    {
    }

    /**
     * Returns the number of action tokens of the specified type that are
     * in this hand.
     */
    public int actionCount (byte actionType)
    {
        int acount = actions.length, tcount = 0;
        for (int ii = 0; ii < acount; ii++) {
            if (actions[ii].code == actionType) {
                tcount++;
            }
        }
        for (int ii = 0; ii < acount; ii++) {
            if (firings[ii].code == actionType) {
                tcount++;
            }
        }
        return tcount;
    }

    /**
     * Resets this skirmish hand to no ops. This is done after the hand is
     * executed in preparation for the next turn.
     */
    public void reset (int turnCounter)
    {
        int acount = actions.length;
        for (int ii = 0; ii < acount; ii++) {
            actions[ii] = SkirmishAction.NOOP;
            firings[ii] = SkirmishAction.NOOP;
        }
        this.turnCounter = turnCounter;
    }
}
