//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import scala.collection.JavaConversions._

import com.threerings.crowd.data.{BodyObject, PlaceObject}
import com.threerings.parlor.game.server.GameManager
import com.threerings.parlor.turn.server.{TurnGameManager, TurnGameManagerDelegate}
import com.threerings.toybox.data.ToyBoxGameConfig

import Reversi.Piece

/**
 * Handles the server side of the game.
 */
class ReversiManager extends GameManager with TurnGameManager
{
  /** Called when a client sends a request to place a piece on the board. */
  def placePiece (player :BodyObject, piece :Piece)  {
    // update our logic with the current state of the board
    val logic = Reversi.logic(_revobj.pieces)

    // make sure it's this player's turn
    val pidx = _turndel.getTurnHolderIndex
    if (_playerOids(pidx) != player.getOid) {
      println("Requested to place piece by non-turn holder [who=" + player.who +
              ", turnHolder=" + _revobj.turnHolder + "].")

    // make sure this is a legal move
    } else if (logic.isLegalMove(piece)) {
      // place this piece on the board
      _revobj.placePiece(piece)
      // have our logic figure out which pieces need flipping
      logic.flipPieces(piece, _revobj)
      // and finally end the turn
      _turndel.endTurn

    } else {
      println("Received illegal move request [who=" + player.who + ", piece=" + piece + "].")
    }
  }

  // from interface TurnGameManager
  def turnWillStart {
    // nothing to do here
  }

  // from interface TurnGameManager
  def turnDidStart {
    // nothing to do here
  }

  // from interface TurnGameManager
  def turnDidEnd {
    // if neither player has legal moves, the game is over
    val logic = Reversi.logic(_revobj.pieces)
    if (!logic.hasLegalMoves(Reversi.Black) && !logic.hasLegalMoves(Reversi.White)) endGame
  }

  override def createPlaceObject :PlaceObject = new ReversiObject

  override protected def gameWillStart {
    super.gameWillStart

    // start the game with the standard arrangement of pieces
    STARTERS.foreach(_revobj.placePiece)
  }

  override protected def assignWinners (winners :Array[Boolean]) {
    super.assignWinners(winners)

    // count up the number of black and white pieces
    val counts = _revobj.pieces.groupBy(_.owner).mapValues(_.size)

    // now set a boolean indicating which player is the winner (note that if it is a tie, we want
    // to set both values to true)
    val (p1c, p2c) = (_revobj.getColor(0), _revobj.getColor(1))
    winners(0) = (counts(p1c) >= counts(p2c))
    winners(1) = (counts(p2c) >= counts(p1c))
  }

  /** Provides a casted reference to our game object. */
  protected lazy val _revobj = _gameobj.asInstanceOf[ReversiObject]

  /** Handles our turn based game flow. */
  protected var _turndel = new TurnGameManagerDelegate {
    override def setNextTurnHolder {
      _turnIdx = Reversi.logic(_revobj.pieces).getNextTurnHolderIndex(_revobj, _turnIdx)
    }
  }
  /* ctor */ { addDelegate(_turndel) }

  /** The starting set of pieces. */
  val STARTERS = Seq(Piece(-1, Reversi.Black, 3, 3), Piece(-1, Reversi.White, 3, 4),
                     Piece(-1, Reversi.Black, 4, 4), Piece(-1, Reversi.White, 4, 3))
}
