//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import java.awt.{AlphaComposite, Composite, Graphics2D}

/**
 * Displays a "potential move" cursor to the player.
 */
class CursorSprite extends PieceSprite(Reversi.Piece(-1, Reversi.None, -1, -1))
{
  def setColor (color :Reversi.Color) {
    updatePiece(_piece.copy(owner = color))
  }

  def setPosition (x :Int, y :Int, logic :Reversi.Logic) {
    updatePiece(_piece.copy(x = x, y = y))
    _legal = logic.isLegalMove(_piece)
  }

  def getPiece :Reversi.Piece = _piece

  override def paint (gfx :Graphics2D) {
    if (_legal) {
      val ocomp = gfx.getComposite
      gfx.setComposite(_comp)
      super.paint(gfx)
      gfx.setComposite(ocomp)
    }
  }

  protected val _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
  protected var _legal = false
}
