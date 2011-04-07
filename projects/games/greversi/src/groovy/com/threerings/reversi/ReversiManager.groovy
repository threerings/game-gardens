//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.reversi;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

// import com.threerings.parlor.turn.server.TurnGameManager;
// import com.threerings.parlor.turn.server.TurnGameManagerDelegate;

import com.threerings.groovy.server.GroovyGameManager;

/**
 * Manages the server side of a Reversi game.
 */
public class ReversiManager extends GroovyGameManager
    implements TurnGameManager
{
    public ReversiManager ()
    {
//         addDelegate(_turndel = new TurnGameManagerDelegate(this));
    }

    /**
     * Called by the client when a player makes their move.
     */
    public void placePiece (BodyObject player, ReversiPiece piece)
    {
        // TODO: make sure it's their turn, they've requested a legal move,
        // etc.

        _plobj.addToPieces(piece);

        // now check to see if any other pieces need flipping
    }

//     // from interface TurnGameManager
//     public void turnWillStart ()
//     {
//     }

//     // from interface TurnGameManager
//     public void turnDidStart ()
//     {
//     }

//     // from interface TurnGameManager
//     public void turnDidEnd ()
//     {
//     }

    // @Override // from GameManager
    protected PlaceObject createPlaceObject ()
    {
        return new ReversiObject();
    }

//     protected TurnGameManagerDelegate _turndel;
}
