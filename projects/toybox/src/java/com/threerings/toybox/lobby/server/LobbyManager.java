//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.gamegardens.com/code/
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

package com.threerings.toybox.lobby.server;

import com.threerings.crowd.server.PlaceManager;

import com.threerings.toybox.server.persist.Game;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.lobby.data.LobbyObject;

import static com.threerings.toybox.lobby.Log.log;

/**
 * Takes care of the server side of a particular lobby.
 */
public class LobbyManager extends PlaceManager
{
    /**
     * Configures this lobby manager with its game record.
     */
    public void setGame (Game game)
    {
        _game = game;
        log.info("Lobby up and running for '" +
                 _lconfig.getGameDefinition().ident + "'.");
    }

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        _lconfig = (LobbyConfig)_config;
    }

    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return LobbyObject.class;
    }

    /** A casted reference to our place config. */
    protected LobbyConfig _lconfig;

    /** The game record for the game that we're matchmaking. */
    protected Game _game;
}
