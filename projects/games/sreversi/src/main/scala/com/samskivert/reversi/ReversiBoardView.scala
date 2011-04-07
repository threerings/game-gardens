//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import scala.collection.JavaConversions._

import java.awt.{Color, Component, Dimension, Font, Graphics2D, Rectangle}
import java.awt.event.{MouseEvent, MouseMotionAdapter, MouseAdapter}
import java.util.HashMap

import com.samskivert.swing.Label

import com.threerings.media.VirtualMediaPanel
import com.threerings.media.animation.FloatingTextAnimation

import com.threerings.presents.dobj.{EntryAddedEvent, EntryRemovedEvent, EntryUpdatedEvent}
import com.threerings.presents.dobj.{DSet, SetListener}

import com.threerings.parlor.media.ScoreAnimation

import com.threerings.toybox.util.ToyBoxContext

import com.threerings.crowd.client.PlaceView
import com.threerings.crowd.data.PlaceObject

/**
 * Displays the main game interface (the board).
 */
class ReversiBoardView (_ctx :ToyBoxContext, _ctrl :ReversiController)
  extends VirtualMediaPanel(_ctx.getFrameManager) with PlaceView
{
  /** Activates "placing" mode which allows the user to place a piece of the specified color. */
  def setPlacingMode (color :Reversi.Color) {
    // if we're running in the unit test, we won't have a game object
    if (_gameobj != null) {
      // update our logic with the current board state
      _logic = Reversi.logic(_gameobj.pieces)
    }

    if (color != Reversi.None) {
      _cursor.setColor(color)
      addSprite(_cursor)
      addMouseListener(_clicker)
      addMouseMotionListener(_mover)
    } else if (isManaged(_cursor)) {
      removeSprite(_cursor)
      removeMouseListener(_clicker)
      removeMouseMotionListener(_mover)
    }
  }

  /** Floats the supplied text over the board. */
  def displayFloatingText (text :String) {
    val label = ScoreAnimation.createLabel(
      text, Color.white, new Font("Helvetica", Font.BOLD, 48), this :Component)
    val lx = (getWidth() - label.getSize().width)/2
    val ly = (getHeight() - label.getSize().height)/2
    addAnimation(new FloatingTextAnimation(label, lx, ly))
  }

  // from interface PlaceView
  def willEnterPlace (plobj :PlaceObject) {
    _gameobj = plobj.asInstanceOf[ReversiObject]
    _gameobj.addListener(_plistener)

    // create sprites for all pieces currently on the board
    _gameobj.pieces.map(addPieceSprite)

    // temporary hackery to allow us to place a piece
    setPlacingMode(Reversi.Black)
  }

  // from interface PlaceView
  def didLeavePlace (plobj :PlaceObject) {
    _gameobj.removeListener(_plistener)
    _gameobj = null
  }

  override def getPreferredSize =
    new Dimension(_size.width * PieceSprite.SIZE + 1, _size.height * PieceSprite.SIZE + 1)

  /** Adds a sprite to the board for the supplied piece. */
  protected def addPieceSprite (piece :Reversi.Piece) {
    val sprite = new PieceSprite(piece)
    _sprites += (piece.getKey -> sprite)
    addSprite(sprite)
  }

  override protected def paintBehind (gfx :Graphics2D, dirtyRect :Rectangle) {
    super.paintBehind(gfx, dirtyRect)

    // fill in our background color
    gfx.setColor(Color.lightGray)
    gfx.fill(dirtyRect)

    // draw our grid
    gfx.setColor(Color.black)
    for (yy <- 0 to _size.height) {
      val ypos = yy * PieceSprite.SIZE
      gfx.drawLine(0, ypos, PieceSprite.SIZE * _size.width, ypos)
    }
    for (xx <- 0 to _size.width) {
      val xpos = xx * PieceSprite.SIZE
      gfx.drawLine(xpos, 0, xpos, PieceSprite.SIZE * _size.height)
    }
  }

  protected val _plistener = new SetListener[Reversi.Piece] {
    def entryAdded (event :EntryAddedEvent[Reversi.Piece]) {
      if (event.getName == ReversiObject.PIECES) {
        // add a sprite for the newly created piece
        addPieceSprite(event.getEntry)
      }
    }
    def entryUpdated (event :EntryUpdatedEvent[Reversi.Piece]) {
      if (event.getName == ReversiObject.PIECES) {
        // update the sprite that is displaying the updated piece
        val piece = event.getEntry
        _sprites(piece.getKey).updatePiece(piece)
      }
    }
    def entryRemoved (event :EntryRemovedEvent[Reversi.Piece]) {
      // nothing to do here
    }
  }

  protected val _clicker = new MouseAdapter {
    override def mousePressed (e :MouseEvent) {
      val piece = _cursor.getPiece
      if (_logic.isLegalMove(piece)) {
        _ctrl.piecePlaced(piece)
        setPlacingMode(Reversi.None)
      }
    }
  }

  protected val _mover = new MouseMotionAdapter {
    override def mouseMoved (e :MouseEvent) {
      val tx = e.getX / PieceSprite.SIZE
      val ty = e.getY / PieceSprite.SIZE
      _cursor.setPosition(tx, ty, _logic)
    }
  }

  /** A reference to our game object. */
  protected var _gameobj :ReversiObject = _

  /** Used to determine legal moves. */
  protected var _logic :Reversi.Logic = Reversi.logic(new DSet)

  /** The size of the Reversi board. */
  protected val _size = new Dimension(8, 8)

  /** Contains a mapping from piece id to the sprite for that piece. */
  protected var _sprites = Map[Comparable[_],PieceSprite]()

  /** Displays a cursor when we're allowing the user to place a piece. */
  protected val _cursor = new CursorSprite
}
