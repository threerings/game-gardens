//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import com.threerings.util.Name

import com.threerings.crowd.client.PlaceView
import com.threerings.crowd.data.PlaceObject
import com.threerings.crowd.util.CrowdContext

import com.threerings.parlor.game.client.GameController
import com.threerings.parlor.turn.client.{TurnGameController, TurnGameControllerDelegate}

import com.threerings.toybox.data.ToyBoxGameConfig
import com.threerings.toybox.util.ToyBoxContext

/**
 * Manages the client side mechanics of the game.
 */
class ReversiController extends GameController with TurnGameController
{
  /** Requests that we leave the game and return to the lobby. */
  def backToLobby {
    _ctx.getLocationDirector.moveBack
  }

  /** Called when a player requests to play a piece. */
  def piecePlaced (piece :Reversi.Piece) {
    // tell the server we want to place our piece here
    _gameobj.manager.invoke("placePiece", piece)
  }

  // from interface TurnGameController
  def turnDidChange (turnHolder :Name) {
    // if it's our turn, activate piece placement
    _panel.bview.setPlacingMode(if (_turndel.isOurTurn) _color else Reversi.None);
  }

  // from PlaceController
  override def willEnterPlace (plobj :PlaceObject) {
    super.willEnterPlace(plobj)

    // get a casted reference to our game object
    _gameobj = plobj.asInstanceOf[ReversiObject]

    // determine our piece color (-1 if we're not a player)
    _color = _gameobj.getColor(_gameobj.getPlayerIndex(_tctx.getUsername))

    // if it's our turn, activate piece placement
    _panel.bview.setPlacingMode(if (_turndel.isOurTurn) _color else Reversi.None)
  }

  // from PlaceController
  override def didLeavePlace (plobj :PlaceObject) {
    super.didLeavePlace(plobj)
    // clear out our game object reference
    _gameobj = null
  }

  // from PlaceController
  override def createPlaceView (ctx :CrowdContext) :PlaceView = {
    _panel = new ReversiPanel(ctx.asInstanceOf[ToyBoxContext], this)
    _panel
  }

  // from GameController
  override protected def gameDidStart () {
    super.gameDidStart
    _panel.gameDidStart(_gameobj)
  }

  // from GameController
  override protected def gameDidEnd () {
    super.gameDidEnd

    // if we are the winner of the game, display some animated text informing us of this fact
    val myidx = _gameobj.getPlayerIndex(_tctx.getUsername)
    val message = if (!_gameobj.isDraw && _gameobj.isWinner(myidx)) "m.you_win" else "m.game_over"
    _panel.bview.displayFloatingText(_tctx.xlate("reversi", message))
  }

  /** Returns our context cast to {@link ToyBoxContext}. */
  protected lazy val _tctx = _ctx.asInstanceOf[ToyBoxContext]

  /** Handles turn-game related stuff. */
  protected val _turndel = new TurnGameControllerDelegate(this)
  /* ctor */ addDelegate(_turndel)

  /** Our game panel. */
  protected var _panel :ReversiPanel = _

  /** Our game distributed object. */
  protected var _gameobj :ReversiObject = _

  /** Our piece color. */
  protected var _color :Reversi.Color = Reversi.None
}
