//
// $Id: SampleObject.dobj,v 1.6 2002/07/16 19:43:15 mdb Exp $

package com.whomever.sample.data;

import com.threerings.parlor.game.GameObject;

/**
 * Maintains the board state of a sample game.
 */
public class SampleObject extends GameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>board</code> field. */
    public static final String BOARD = "board";
    // AUTO-GENERATED: FIELDS END

    /** The board on which the game is played. */
    public SampleBoard board;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>board</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBoard (SampleBoard value)
    {
        SampleBoard ovalue = this.board;
        requestAttributeChange(
            BOARD, value, ovalue);
        this.board = value;
    }
    // AUTO-GENERATED: METHODS END
}
