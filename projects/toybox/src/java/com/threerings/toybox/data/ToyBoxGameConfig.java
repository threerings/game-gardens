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

package com.threerings.toybox.data;

import java.util.List;

import java.util.logging.Level;

import com.threerings.util.StreamableHashMap;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.PartyGameConfig;

import com.threerings.toybox.client.ToyBoxGameConfigurator;

import static com.threerings.toybox.Log.log;

/**
 * Provides a mechanism for communicating game configuration information
 * to ToyBox games in a way that avoids having to load any game code
 * during the match making process.
 */
public class ToyBoxGameConfig extends GameConfig
    implements PartyGameConfig
{
    /** Our configuration parameters. These will be seeded with the
     * defaults from the game definition and then configured by the player
     * in the lobby. */
    public StreamableHashMap params = new StreamableHashMap();

    /** A zero argument constructor used when unserializing. */
    public ToyBoxGameConfig ()
    {
    }

    /** Constructs a game config based on the supplied game definition. */
    public ToyBoxGameConfig (int gameId, GameDefinition gamedef)
    {
        _gameId = gameId;
        _gamedef = gamedef;

        // set the default values for our parameters
        params.put("seats", ((TableMatchConfig)_gamedef.match).startSeats);
        for (int ii = 0; ii < gamedef.params.length; ii++) {
            params.put(gamedef.params[ii].ident,
                       gamedef.params[ii].getDefaultValue());
        }
    }

    // documentation inherited
    public byte getRatingTypeId ()
    {
        return -1; // TODO: make this go away
    }

    // documentation inherited
    public String getGameName ()
    {
        return _gamedef.ident;
    }

    // documentation inherited
    public String getBundleName ()
    {
        return _gamedef.ident;
    }

    // documentation inherited
    public GameConfigurator createConfigurator ()
    {
        return new ToyBoxGameConfigurator();
    }

    // documentation inherited
    public Class getControllerClass ()
    {
        try {
            return Class.forName(_gamedef.controller, true, _loader);
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to instantiate game controller " +
                    "[class=" + _gamedef.controller + "]", e);
            return null;
        }
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return _gamedef.manager;
    }

    // documentation inherited
    public List getDescription ()
    {
        List desc = super.getDescription();
        // we have nothing to add at the moment
        return desc;
    }

    // documentation inherited from interface TableConfig
    public int getMinimumPlayers ()
    {
        return ((TableMatchConfig)_gamedef.match).minSeats;
    }

    // documentation inherited from interface TableConfig
    public int getMaximumPlayers ()
    {
        return ((TableMatchConfig)_gamedef.match).maxSeats;
    }

    // documentation inherited from interface TableConfig
    public int getDesiredPlayers ()
    {
        return (Integer)params.get("seats");
    }

    // documentation inherited from interface TableConfig
    public void setDesiredPlayers (int desiredPlayers)
    {
        params.put("seats", desiredPlayers);
    }

    // documentation inherited from interface TableConfig
    public boolean isPrivateTable ()
    {
        return _isPrivate;
    }

    // documentation inherited from interface TableConfig
    public void setPrivateTable (boolean privateTable)
    {
        _isPrivate = privateTable;
    }

    // documentation inherited from interface PartyGameConfig
    public byte getPartyGameType ()
    {
        return ((TableMatchConfig)_gamedef.match).isPartyGame ?
            FREE_FOR_ALL_PARTY_GAME : NOT_PARTY_GAME;
    }

    /** Returns true if this is a party game, false otherwise. */
    public boolean isPartyGame ()
    {
        return getPartyGameType() != NOT_PARTY_GAME;
    }

    /** Returns the id of the game associated with this config instance. */
    public int getGameId ()
    {
        return _gameId;
    }

    /** Returns the game definition associated with this config instance. */
    public GameDefinition getGameDefinition ()
    {
        return _gamedef;
    }

    /** Our game's unique id. */
    protected int _gameId;

    /** Our game definition. */
    protected GameDefinition _gamedef;

    /** Allows creation of private tables. */
    protected boolean _isPrivate;
}
