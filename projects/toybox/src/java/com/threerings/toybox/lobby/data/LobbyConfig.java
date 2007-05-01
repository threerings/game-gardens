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

package com.threerings.toybox.lobby.data;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.PlaceConfig;

import com.threerings.ezgame.data.GameDefinition;

import com.threerings.toybox.lobby.client.LobbyController;

/**
 * Defines the configuration of a ToyBox match-making lobby.
 */
public class LobbyConfig extends PlaceConfig
{
    /**
     * A default constructor used when unserializing.
     */
    public LobbyConfig ()
    {
    }

    /**
     * Creates the config for a new lobby that will match-make games with
     * the specified configuration.
     */
    public LobbyConfig (int gameId, GameDefinition gamedef)
    {
        _gameId = gameId;
        _gamedef = gamedef;
    }

    // documentation inherited
    public PlaceController createController ()
    {
        return new LobbyController();
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.toybox.lobby.server.LobbyManager";
    }

    /**
     * Returns this game's unique identifier.
     */
    public int getGameId ()
    {
        return _gameId;
    }

    /**
     * Returns the definition of the game we're matchmaking in this lobby.
     */
    public GameDefinition getGameDefinition ()
    {
        return _gamedef;
    }

    // documentation inherited
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        if (buf.length() > 1) {
            buf.append(", ");
        }
        buf.append("gamedef=").append(_gamedef);
    }

    /** The unique id for the game we'll be matchmaking. */
    protected int _gameId;

    /** The definition for the game we'll be matchmaking. */
    protected GameDefinition _gamedef;
}
