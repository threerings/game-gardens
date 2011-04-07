//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import com.threerings.util.Name

import com.threerings.presents.dobj.DSet
import com.threerings.parlor.game.data.GameObject
import com.threerings.parlor.turn.data.TurnGameObject

/**
 * Maintains the shared state of the game.
 */
class ReversiObject extends GameObject with TurnGameObject with ScalaDObject
{
  import ReversiObject._

  /** Contains the pieces on the game board. */
  var pieces = new DSet[Reversi.Piece]

  /** The username of the current turn holder or null. */
  var turnHolder :Name = _

  /** Places the supplied piece onto the board, first assigning it a unique piece id. */
  def placePiece (piece :Reversi.Piece) {
    // assign this piece a new unique piece id and add it to the set
    addToPieces(piece.copy(pieceId = Reversi.nextPieceId))
  }

  // from interface TurnGameObject
  def getTurnHolderFieldName :String = TURN_HOLDER

  // from interface TurnGameObject
  def getTurnHolder = turnHolder

  // from interface TurnGameObject
  def getPlayers = players

  /** Returns the color for the player of the specified index. Currently always maps zero to white
   * and one to black, but this could be randomly configured at the start of each game. */
  def getColor (pidx :Int) = pidx match {
    case 0 => Reversi.White
    case 1 => Reversi.Black
    case _ => Reversi.None
  }

  // AUTO-GENERATED: METHODS START
  /**
   * Requests that the specified entry be added to the
   * <code>pieces</code> set. The set will not change until the event is
   * actually propagated through the system.
   */
  def addToPieces (elem :Reversi.Piece) {
    requestEntryAdd(PIECES, pieces, elem)
  }

  /**
   * Requests that the entry matching the supplied key be removed from
   * the <code>pieces</code> set. The set will not change until the
   * event is actually propagated through the system.
   */
  def removeFromPieces (key :Comparable[_]) {
    requestEntryRemove(PIECES, pieces, key)
  }

  /**
   * Requests that the specified entry be updated in the
   * <code>pieces</code> set. The set will not change until the event is
   * actually propagated through the system.
   */
  def updatePieces (elem :Reversi.Piece) {
    requestEntryUpdate(PIECES, pieces, elem)
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
  def setPieces (value :DSet[Reversi.Piece])
  {
    requestAttributeChange(PIECES, value, this.pieces)
    this.pieces = if (value == null) null else  value.clone
  }

  /**
   * Requests that the <code>turnHolder</code> field be set to the
   * specified value. The local value will be updated immediately and an
   * event will be propagated through the system to notify all listeners
   * that the attribute did change. Proxied copies of this object (on
   * clients) will apply the value change when they received the
   * attribute changed notification.
   */
  def setTurnHolder (value :Name) {
    val ovalue = this.turnHolder
    requestAttributeChange(TURN_HOLDER, value, ovalue)
    this.turnHolder = value
  }
  // AUTO-GENERATED: METHODS END
}

object ReversiObject
{
  // AUTO-GENERATED: FIELDS START
  /** The field name of the <code>pieces</code> field. */
  val PIECES = "pieces"

  /** The field name of the <code>turnHolder</code> field. */
  val TURN_HOLDER = "turnHolder"
  // AUTO-GENERATED: FIELDS END
}
