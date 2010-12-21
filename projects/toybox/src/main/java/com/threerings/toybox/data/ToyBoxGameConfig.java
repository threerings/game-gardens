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

package com.threerings.toybox.data;

import com.google.common.base.Preconditions;

import com.threerings.util.StreamableHashMap;

import com.threerings.crowd.client.PlaceController;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

import com.threerings.toybox.client.ToyBoxGameConfigurator;

/**
 * Provides configuration to ToyBox games.
 */
public class ToyBoxGameConfig extends GameConfig
{
    /** Our configuration parameters. These will be seeded with the defaults from the game
     * definition and then configured by the player in the lobby. */
    public StreamableHashMap<String,Object> params = new StreamableHashMap<String,Object>();

    /** A zero argument constructor used when unserializing. */
    public ToyBoxGameConfig ()
    {
    }

    /** Constructs a game config based on the supplied game definition. */
    public ToyBoxGameConfig (int gameId, GameDefinition gameDef)
    {
        Preconditions.checkNotNull(gameDef, "Missing GameDefinition");

        _gameId = gameId;
        _gameDef = gameDef;

        if (gameDef.params != null) {
            // set the default values for our parameters
            for (int ii = 0; ii < gameDef.params.length; ii++) {
                params.put(gameDef.params[ii].ident, gameDef.params[ii].getDefaultValue());
            }
        }
    }

    /**
     * Returns the non-changing metadata that defines this game.
     */
    public GameDefinition getGameDefinition ()
    {
        return _gameDef;
    }

    /** Returns true if this is a party game, false otherwise. */
    public boolean isPartyGame ()
    {
        return getMatchType() == PARTY;
    }

    @Override // from GameConfig
    public int getGameId ()
    {
        return _gameId;
    }

    @Override // from GameConfig
    public String getGameIdent ()
    {
        return _gameDef.ident;
    }

    @Override // from GameConfig
    public int getMatchType ()
    {
        return _gameDef.match.getMatchType();
    }

    @Override // from GameConfig
    public GameConfigurator createConfigurator ()
    {
        return new ToyBoxGameConfigurator();
    }

    @Override // from PlaceConfig
    public PlaceController createController ()
    {
        String ctrl = getGameDefinition().controller;
        if (ctrl == null) {
            throw new IllegalStateException("Game definition missing controller [gdef=" +
                getGameDefinition() + "]");
        }
        try {
            return (PlaceController) Class.forName(ctrl).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override // from PlaceConfig
    public String getManagerClassName ()
    {
        return _gameDef.manager;
    }

    /** Our game's unique id. */
    protected int _gameId;

    /** Our game definition. */
    protected GameDefinition _gameDef;
}
