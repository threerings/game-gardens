//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/jreversi/LICENSE

package com.samskivert.reversi;

import com.threerings.util.Name;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;

import com.threerings.toybox.util.ToyBoxContext;

/**
 * Manages the client side mechanics of the game.
 */
public class ReversiController extends GameController
    implements TurnGameController
{
    public ReversiController ()
    {
        addDelegate(_turndel = new TurnGameControllerDelegate(this));
    }

    /**
     * Requests that we leave the game and return to the lobby.
     */
    public void backToLobby ()
    {
        _ctx.getLocationDirector().moveBack();
    }

    /**
     * Called when a player requests to play a piece during their turn.
     */
    public void piecePlaced (ReversiObject.Piece piece)
    {
        // tell the server we want to place our piece here
        _gameobj.manager.invoke("placePiece", piece);
    }

    // from interface TurnGameController
    public void turnDidChange (Name turnHolder)
    {
        // if it's our turn, activate piece placement
        _panel.bview.setPlacingMode(_turndel.isOurTurn() ? _color : -1);
    }

    @Override // from PlaceController
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _gameobj = (ReversiObject)plobj;

        // determine our piece color (-1 if we're not a player)
        _color = _gameobj.getPlayerIndex(((ToyBoxContext)_ctx).getUsername());

        // if it's our turn, activate piece placement
        _panel.bview.setPlacingMode(_turndel.isOurTurn() ? _color : -1);
    }

    @Override // from PlaceController
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        // clear out our game object reference
        _gameobj = null;
    }

    @Override // from PlaceController
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        _panel = new ReversiPanel((ToyBoxContext)ctx, this);
        return _panel;
    }

    @Override // from GameController
    protected void gameDidStart ()
    {
        super.gameDidStart();

        // here we can set up anything that should happen at the start of the
        // game
    }

    @Override // from GameController
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // if we are the winner of the game, display some animated text
        // informing us of this fact
        ToyBoxContext tctx = (ToyBoxContext)_ctx;
        String message = (!_gameobj.isDraw() && _gameobj.isWinner(_color)) ?
            "m.you_win" : "m.game_over";
        _panel.bview.displayFloatingText(tctx.xlate("reversi", message));
    }

    /** Handles turn-game related stuff. */
    protected TurnGameControllerDelegate _turndel;

    /** Our game panel. */
    protected ReversiPanel _panel;

    /** Our game distributed object. */
    protected ReversiObject _gameobj;

    /** Our piece color. */
    protected int _color;
}
