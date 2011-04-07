//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import scala.collection.JavaConversions._

import com.threerings.io.Streamable
import com.threerings.presents.dobj.DSet

/**
 * Contains some constants and logic shared by client and server.
 */
object Reversi
{
  /** Represents the color of a piece on the board. This is a string because we need to stream
   * Color values to and from the server, and Presents doesn't magically support Scala enums like
   * it does Java's. */
  type Color = String
  val None = "None"
  val White = "White"
  val Black = "Black"

  /** Mapping from color to opponent color. */
  val Opponent = Map[Color,Color](Black -> White, White -> Black)

  /** Models a single piece on the board. We store a set of pieces in ReversiObject. */
  case class Piece (pieceId :Int, owner :Color, x :Int, y :Int) extends DSet.Entry {
    /** A constructor needed for unserialization. */
    def this () = this(-1, None, -1, -1)
    // from DSet.Entry
    def getKey :Comparable[_] = Integer.valueOf(pieceId)
  }

  /** Returns a monotonically increasing id to assign to a piece. */
  def nextPieceId :Int = { _nextPieceId += 1 ; _nextPieceId }

  /** Returns a logic configured with the supplied board state. */
  def logic (pieces :DSet[Piece]) = new Logic(8, pieces)

  /** Performs analysis of Reversi board state to determine move legality and piece flipping. */
  class Logic (_size :Int, pieces :DSet[Piece]) {
    /** Returns the index into the {@link ReversiObject#players} array of the player to whom control
     * should transition. */
    def getNextTurnHolderIndex (revobj :ReversiObject, curTurnIdx :Int) :Int = {
      // if the next player can move, they're up
      if (hasLegalMoves(revobj.getColor(1-curTurnIdx))) 1-curTurnIdx
      // otherwise see if the current player can still move
      else if (hasLegalMoves(revobj.getColor(curTurnIdx))) curTurnIdx
      // otherwise the game is over
      else -1
    }

    /** Returns true if the supplied piece represents a legal move for the owning player. */
    def isLegalMove (piece :Piece) = {
      // disallow moves on out of bounds and already occupied spots
      if (!inBounds(piece.x, piece.y) || getColor(piece.x, piece.y) != None) false
      else {
        val ocolor = Opponent(piece.owner)
        def isCapture (state :State, x :Int, y :Int, color :Color) = state.step(color, ocolor)
        // determine whether this play would result in capture of pieces of the opposite color; we
        // check in each possible direction from the specified position
        Deltas.exists(d => fold(piece, d._1, d._2, Start, isCapture) == Success)
      }
    }

    /** Returns true if the player with the specified color has legal moves. */
    def hasLegalMoves (color :Color) = {
      // search every empty board position for a legal move
      val empties = for (yy <- 0 until _size; xx <- 0 until _size; if (getColor(xx, yy) == None))
                    yield Piece(-1, color, xx, yy)
      empties.exists(isLegalMove)
    }

    /** Determines which pieces should be flipped based on the placement of the specified piece
     * onto the board. The pieces in question are changed to the appropriate color and updated in
     * the game object. */
    def flipPieces (placed :Piece, gameobj :ReversiObject) {
      // this just accumulates the pieces in a row from the start point to the edge of the board
      def accum (pieces :List[Piece], x :Int, y :Int, color :Color) =
        Piece(-1, color, x, y) :: pieces

      // this returns the pieces to be flipped, ensuring that the supplied set of pieces has the
      // form: other+, self, .*
      val ocolor = Opponent(placed.owner)
      def filterFlips (pieces :List[Piece]) :List[(Int,Int)] = {
        val toFlip = pieces takeWhile(_.owner == ocolor)
        val rest = pieces drop(toFlip.length)
        if (toFlip.isEmpty || rest.isEmpty || rest.head.owner != placed.owner) Nil
        else toFlip map(p => (p.x, p.y))
      }

      // accumulate all of the tiles that need flipping
      val toFlip = Set() ++ Deltas.flatMap(
        d => filterFlips(fold(placed, d._1, d._2, Nil, accum).reverse))

      // and update the corresponding pieces in the game object (in a transaction so that we emit a
      // single event with potentially many updates)
      try {
        gameobj.startTransaction
        for (p <- gameobj.pieces.toSeq; if (toFlip(Pair(p.x, p.y)))) {
          gameobj.updatePieces(p.copy(owner = Opponent(p.owner)))
        }
      } finally gameobj.commitTransaction
    }

    protected final def getColor (x :Int, y :Int) = _state(y * _size + x);
    protected final def inBounds (x :Int, y :Int) = (x >= 0 && y >= 0 && x < _size && y < _size)

    // a helper to fold a function over the sequence of pieces that extend from the requested
    // location to the end of the board in a direction defined by a delta x, y
    def fold[A] (piece :Piece, dx :Int, dy :Int, z :A, f :((A, Int, Int, Color) => A)) = {
      def step (x :Int, y :Int, a :A) :A =
        if (inBounds(x, y)) step(x+dx, y+dy, f(a, x, y, getColor(x, y))) else a
      step(piece.x+dx, piece.y+dy, z)
    }

    protected val _state = Array.fill(_size*_size)(None)
    // initialize the state array from the supplied pieces
    for (p <- pieces) _state(p.y * _size + p.x) = p.owner
  }

  /** Used to assign ids to pieces. */
  protected var _nextPieceId = 0

  /** Used by the logic class to compute legal moves. */
  trait State {
    def step (color :Color, ocolor :Color) :State = this
  }
  object Success extends State
  object Fail extends State
  object Start extends State {
    override def step (color :Color, ocolor :Color) =
      if (color == ocolor) They else Fail
  }
  object They extends State {
    override def step (color :Color, ocolor :Color) =
      if (color == ocolor) They else if (color == Opponent(ocolor)) Success else Fail
  }

  /** Used to traverse the baord. */
  protected val Deltas =
    List((-1, -1), (0, -1), (1, -1), (-1, 0), (1, 0), (-1, 1), (0, 1), (1, 1))
}
