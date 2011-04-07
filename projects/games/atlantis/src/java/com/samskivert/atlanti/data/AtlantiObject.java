//
// Atlantis - A tile laying game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/atlantis/LICENSE

package com.samskivert.atlanti.data;

import javax.annotation.Generated;
import com.threerings.util.Name;
import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;

/**
 * The distributed object used to maintain state for the game.
 */
public class AtlantiObject extends GameObject
    implements TurnGameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>turnHolder</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>tiles</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TILES = "tiles";

    /** The field name of the <code>currentTile</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CURRENT_TILE = "currentTile";

    /** The field name of the <code>piecens</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String PIECENS = "piecens";

    /** The field name of the <code>scores</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String SCORES = "scores";
    // AUTO-GENERATED: FIELDS END

    /** The username of the current turn holder. */
    public Name turnHolder;

    /** A set containing all of the tiles that are in play in this
     * game. */
    public DSet<AtlantiTile> tiles = new DSet<AtlantiTile>();

    /** The tile being placed by the current turn holder. This value is
     * only valid while it is someone's turn. */
    public AtlantiTile currentTile = AtlantiTile.STARTING_TILE;

    /** A set containing all of the piecens that are placed on the
     * board. */
    public DSet<Piecen> piecens = new DSet<Piecen>();

    /** The scores for each player. */
    public int[] scores;

    // documentation inherited from interface
    public Name[] getPlayers ()
    {
        return players;
    }

    // documentation inherited from interface
    public String getTurnHolderFieldName ()
    {
        return TURN_HOLDER;
    }

    // documentation inherited from interface
    public Name getTurnHolder ()
    {
        return turnHolder;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTurnHolder (Name value)
    {
        Name ovalue = this.turnHolder;
        requestAttributeChange(
            TURN_HOLDER, value, ovalue);
        this.turnHolder = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>tiles</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToTiles (AtlantiTile elem)
    {
        requestEntryAdd(TILES, tiles, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>tiles</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromTiles (Comparable<?> key)
    {
        requestEntryRemove(TILES, tiles, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>tiles</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateTiles (AtlantiTile elem)
    {
        requestEntryUpdate(TILES, tiles, elem);
    }

    /**
     * Requests that the <code>tiles</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTiles (DSet<AtlantiTile> value)
    {
        requestAttributeChange(TILES, value, this.tiles);
        DSet<AtlantiTile> clone = (value == null) ? null : value.clone();
        this.tiles = clone;
    }

    /**
     * Requests that the <code>currentTile</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setCurrentTile (AtlantiTile value)
    {
        AtlantiTile ovalue = this.currentTile;
        requestAttributeChange(
            CURRENT_TILE, value, ovalue);
        this.currentTile = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>piecens</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToPiecens (Piecen elem)
    {
        requestEntryAdd(PIECENS, piecens, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>piecens</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromPiecens (Comparable<?> key)
    {
        requestEntryRemove(PIECENS, piecens, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>piecens</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updatePiecens (Piecen elem)
    {
        requestEntryUpdate(PIECENS, piecens, elem);
    }

    /**
     * Requests that the <code>piecens</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPiecens (DSet<Piecen> value)
    {
        requestAttributeChange(PIECENS, value, this.piecens);
        DSet<Piecen> clone = (value == null) ? null : value.clone();
        this.piecens = clone;
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
    public void setScores (int[] value)
    {
        int[] ovalue = this.scores;
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
    public void setScoresAt (int value, int index)
    {
        int ovalue = this.scores[index];
        requestElementUpdate(
            SCORES, index, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.scores[index] = value;
    }
    // AUTO-GENERATED: METHODS END
}
