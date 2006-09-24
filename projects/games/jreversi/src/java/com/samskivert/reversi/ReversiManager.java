//
// $Id: SampleManager.java,v 1.21 2004/08/27 18:51:26 mdb Exp $

package com.samskivert.reversi;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.turn.server.TurnGameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;

import com.threerings.toybox.data.ToyBoxGameConfig;

/**
 * Handles the server side of the game.
 */
public class ReversiManager extends GameManager
    implements TurnGameManager
{
    public ReversiManager ()
    {
        // we're a turn based game, so we use a turn game manager delegate
        addDelegate(_turndel = new TurnGameManagerDelegate(this) {
            protected void setNextTurnHolder () {
                _turnIdx = _gameobj.getNextTurnHolderIndex(_turnIdx);
            }
        });
    }

    /**
     * Called when a client sends a request to place a piece on the board.
     */
    public void placePiece (BodyObject player, ReversiObject.Piece piece)
    {
        // for now we just blindly add the piece to the board, yee haw!
        _gameobj.addToPieces(piece);
    }

    // from interface TurnGameManager
    public void turnWillStart ()
    {
        // nothing to do here
    }

    // from interface TurnGameManager
    public void turnDidStart ()
    {
        // nothing to do here
    }

    // from interface TurnGameManager
    public void turnDidEnd ()
    {
        // if neither player has legal moves, the game is over
        if (!_gameobj.hasLegalMoves(0) && !_gameobj.hasLegalMoves(1)) {
            endGame();
        }
    }

    @Override // from PlaceManager
    public void didInit ()
    {
        super.didInit();

        // get a casted reference to our game configuration
        _gameconf = (ToyBoxGameConfig)_config;

        // this is called when our manager is created but before any
        // game-specific actions take place; we don't yet have our game object
        // at this point but we do have our game configuration
    }

    @Override // from PlaceManager
    public void didStartup ()
    {
        super.didStartup();

        // grab our own casted game object reference
        _gameobj = (ReversiObject)super._gameobj;

        // this method is called after we have created our game object but
        // before we do any game related things
    }

    @Override // from PlaceManager
    public void didShutdown ()
    {
        super.didShutdown();

        // this is called right before we finally disappear for good
    }

    @Override // from PlaceManager
    protected PlaceObject createPlaceObject ()
    {
        return new ReversiObject();
    }

    @Override // from GameManager
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // when all the players have entered the game room, the game is
        // automatically started and this method is called just before the
        // event is delivered to the clients that will start the game

        // this is the place to do any pre-game setup that needs to be done
        // each time a game is started rather than just once at the very
        // beginning (those sorts of things should be done in didStartup())
    }

    @Override // from GameManager
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // this is called after the game has ended. somewhere in the game
        // manager a call to endGame() should be made when the manager knows
        // the game to be over and that will trigger the end-of-game processing
        // including calling this method
    }

    /** Our game object. */
    protected ReversiObject _gameobj;

    /** Our game configuration. */
    protected ToyBoxGameConfig _gameconf;

    /** Handles our turn based game flow. */
    protected TurnGameManagerDelegate _turndel;
}
