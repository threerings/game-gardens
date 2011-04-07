//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.data;

import javax.annotation.Generated;
import com.threerings.parlor.game.data.GameObject;

/**
 * Contains the shared data used in the game.
 */
public class SagashiObject extends GameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>service</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String SERVICE = "service";

    /** The field name of the <code>board</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String BOARD = "board";

    /** The field name of the <code>secondsUntil</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String SECONDS_UNTIL = "secondsUntil";

    /** The field name of the <code>scores</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String SCORES = "scores";

    /** The field name of the <code>allWords</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String ALL_WORDS = "allWords";
    // AUTO-GENERATED: FIELDS END

    /** Our game invocation service. */
    public SagashiMarshaller service;

    /** The letters on the board. */
    public SagashiBoard board;

    /** The number of seconds until something exciting will happen (end of
     * current game or start of next). */
    public int secondsUntil;

    /** Contains score records for every scoring occupant. */
    public SagashiScore[] scores;

    /** A list of all words found during the round. */
    public String[] allWords;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>service</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setService (SagashiMarshaller value)
    {
        SagashiMarshaller ovalue = this.service;
        requestAttributeChange(
            SERVICE, value, ovalue);
        this.service = value;
    }

    /**
     * Requests that the <code>board</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setBoard (SagashiBoard value)
    {
        SagashiBoard ovalue = this.board;
        requestAttributeChange(
            BOARD, value, ovalue);
        this.board = value;
    }

    /**
     * Requests that the <code>secondsUntil</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setSecondsUntil (int value)
    {
        int ovalue = this.secondsUntil;
        requestAttributeChange(
            SECONDS_UNTIL, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.secondsUntil = value;
    }

    /**
     * Requests that the <code>scores</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setScores (SagashiScore[] value)
    {
        SagashiScore[] ovalue = this.scores;
        requestAttributeChange(
            SCORES, value, ovalue);
        this.scores = (value == null) ? null : value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>scores</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setScoresAt (SagashiScore value, int index)
    {
        SagashiScore ovalue = this.scores[index];
        requestElementUpdate(
            SCORES, index, value, ovalue);
        this.scores[index] = value;
    }

    /**
     * Requests that the <code>allWords</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setAllWords (String[] value)
    {
        String[] ovalue = this.allWords;
        requestAttributeChange(
            ALL_WORDS, value, ovalue);
        this.allWords = (value == null) ? null : value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>allWords</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setAllWordsAt (String value, int index)
    {
        String ovalue = this.allWords[index];
        requestElementUpdate(
            ALL_WORDS, index, value, ovalue);
        this.allWords[index] = value;
    }
    // AUTO-GENERATED: METHODS END
}
