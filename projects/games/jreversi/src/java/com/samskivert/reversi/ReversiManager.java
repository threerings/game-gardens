//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/jreversi/LICENSE

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
        addDelegate(_turndel = new TurnGameManagerDelegate() {
            @Override
            protected void setNextTurnHolder () {
                _logic.setState(_gameobj.pieces);
                _turnIdx = _logic.getNextTurnHolderIndex(_turnIdx);
            }
        });
    }

    /**
     * Called when a client sends a request to place a piece on the board.
     */
    public void placePiece (BodyObject player, ReversiObject.Piece piece)
    {
        // update our logic with the current state of the board
        _logic.setState(_gameobj.pieces);

        // make sure it's this player's turn
        int pidx = _turndel.getTurnHolderIndex();
        if (_playerOids[pidx] != player.getOid()) {
            System.err.println("Requested to place piece by non-turn holder " +
                               "[who=" + player.who() +
                               ", turnHolder=" + _gameobj.turnHolder + "].");

        // make sure this is a legal move
        } else if (_logic.isLegalMove(piece)) {
            // place this piece on the board
            _gameobj.placePiece(piece);
            // have our logic figure out which pieces need flipping
            _logic.flipPieces(piece, _gameobj);
            // and finally end the turn
            _turndel.endTurn();

        } else {
            System.err.println("Received illegal move request " +
                               "[who=" + player.who() +
                               ", piece=" + piece + "].");
        }
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
        _logic.setState(_gameobj.pieces);
        if (!_logic.hasLegalMoves(ReversiObject.BLACK) &&
            !_logic.hasLegalMoves(ReversiObject.WHITE)) {
            endGame();
        }
    }

    @Override // from PlaceManager
    public void didInit ()
    {
        super.didInit();

        // get a casted reference to our game configuration
        _gameconf = (ToyBoxGameConfig)_config;

        // create our game logic instance
        _logic = new ReversiLogic(8); // TODO: get board size from config
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

        // start the game with the standard arrangement of pieces
        for (int ii = 0; ii < STARTERS.length; ii += 3) {
            ReversiObject.Piece piece = new ReversiObject.Piece();
            piece.x = STARTERS[ii];
            piece.y = STARTERS[ii+1];
            piece.owner = STARTERS[ii+2];
            _gameobj.placePiece(piece);
        }
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

    @Override // from GameManager
    protected void assignWinners (boolean[] winners)
    {
        super.assignWinners(winners);

        // count up the number of black and white pieces
        int[] counts = new int[2];
        for (ReversiObject.Piece piece : _gameobj.pieces) {
            counts[piece.owner]++;
        }

        // now set a boolean indicating which player is the winner (note that
        // if it is a tie, we want to set both values to true)
        winners[0] = (counts[0] >= counts[1]);
        winners[1] = (counts[1] >= counts[0]);
    }

    /** Our game object. */
    protected ReversiObject _gameobj;

    /** Used to determine legality of moves, etc. */
    protected ReversiLogic _logic;

    /** Our game configuration. */
    protected ToyBoxGameConfig _gameconf;

    /** Handles our turn based game flow. */
    protected TurnGameManagerDelegate _turndel;

    /** The starting set of pieces. */
    protected static final int[] STARTERS = {
        3, 3, ReversiObject.BLACK,
        3, 4, ReversiObject.WHITE,
        4, 4, ReversiObject.BLACK,
        4, 3, ReversiObject.WHITE,
    };
}
