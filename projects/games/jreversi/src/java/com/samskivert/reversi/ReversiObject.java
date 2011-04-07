//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/jreversi/LICENSE

package com.samskivert.reversi;

import javax.annotation.Generated;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;

/**
 * Maintains the shared state of the game.
 */
public class ReversiObject extends GameObject
    implements TurnGameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>pieces</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String PIECES = "pieces";

    /** The field name of the <code>turnHolder</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TURN_HOLDER = "turnHolder";
    // AUTO-GENERATED: FIELDS END

    /** The index into the {@link #players} array of the black player. */
    public static final int BLACK = 0;

    /** The index into the {@link #players} array of the white player. */
    public static final int WHITE = 1;

    /** Represents a single piece on the game board. */
    public static class Piece implements DSet.Entry
    {
        public int pieceId;
        public int owner;
        public int x, y;

        public Comparable<?> getKey () {
            return pieceId;
        }
    }

    /** Contains the pieces on the game board. */
    public DSet<Piece> pieces = new DSet<Piece>();

    /** The username of the current turn holder or null. */
    public Name turnHolder;

    /**
     * Places the supplied piece onto the board, first assigning it a unique
     * piece id.
     */
    public void placePiece (Piece piece)
    {
        // assign this piece a new unique piece id
        piece.pieceId = ++_nextPieceId;

        // add this new piece to the set
        addToPieces(piece);
    }

    // from interface TurnGameObject
    public String getTurnHolderFieldName ()
    {
        return TURN_HOLDER;
    }

    // from interface TurnGameObject
    public Name getTurnHolder ()
    {
        return turnHolder;
    }

    // from interface TurnGameObject
    public Name[] getPlayers ()
    {
        return players;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToPieces (ReversiObject.Piece elem)
    {
        requestEntryAdd(PIECES, pieces, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>pieces</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromPieces (Comparable<?> key)
    {
        requestEntryRemove(PIECES, pieces, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updatePieces (ReversiObject.Piece elem)
    {
        requestEntryUpdate(PIECES, pieces, elem);
    }

    /**
     * Requests that the <code>pieces</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPieces (DSet<ReversiObject.Piece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        DSet<ReversiObject.Piece> clone = (value == null) ? null : value.clone();
        this.pieces = clone;
    }

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
    // AUTO-GENERATED: METHODS END

    /** Used to assign ids to pieces. */
    protected transient int _nextPieceId;
}
