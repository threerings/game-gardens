//
// $Id$

package com.samskivert.reversi;

import com.threerings.presents.dobj.DSet;
import com.threerings.parlor.game.data.GameObject;

/**
 * Maintains the shared state of the game.
 */
public class ReversiObject extends GameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>pieces</code> field. */
    public static final String PIECES = "pieces";
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

        public Comparable getKey () {
            return pieceId;
        }
    }

    /** Contains the pieces on the game board. */
    public DSet<Piece> pieces = new DSet<Piece>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToPieces (ReversiObject.Piece elem)
    {
        requestEntryAdd(PIECES, pieces, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>pieces</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPieces (Comparable key)
    {
        requestEntryRemove(PIECES, pieces, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>pieces</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
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
    public void setPieces (DSet<com.samskivert.reversi.ReversiObject.Piece> value)
    {
        requestAttributeChange(PIECES, value, this.pieces);
        @SuppressWarnings("unchecked") DSet<com.samskivert.reversi.ReversiObject.Piece> clone =
            (value == null) ? null : value.typedClone();
        this.pieces = clone;
    }
    // AUTO-GENERATED: METHODS END
}
