//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import java.awt.{Color, Graphics2D}

import com.threerings.media.sprite.Sprite

/**
 * Displays a piece on the board view.
 */
class PieceSprite (piece :Reversi.Piece) extends Sprite(PieceSprite.SIZE, PieceSprite.SIZE)
{
  /* ctor */ updatePiece(piece)

  /** Called when the piece we are displaying has been updated. */
  def updatePiece (piece :Reversi.Piece) {
    // keep track of our piece
    _piece = piece;

    // set our location based on the location of the piece
    setLocation(_piece.x * PieceSprite.SIZE, _piece.y * PieceSprite.SIZE)

    // force a redraw in case our color changed but not our location
    invalidate()
  }

  override def paint (gfx :Graphics2D) {
    // set our color depending on the player that owns this piece
    gfx.setColor(if (_piece.owner == Reversi.Black) Color.darkGray else Color.white)

    // draw a filled in circle in our piece color
    val (px, py) = (_bounds.x + 3, _bounds.y + 3)
    val (pwid, phei) = (_bounds.width - 6, _bounds.height - 6)
    gfx.fillOval(px, py, pwid, phei)

    // then outline that oval in black
    gfx.setColor(Color.black)
    gfx.drawOval(px, py, pwid, phei)
  }

  protected var _piece :Reversi.Piece = _
}

object PieceSprite
{
  /** The dimensions of our sprite in pixels. */
  var SIZE = 64;
}
