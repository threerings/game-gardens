//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
