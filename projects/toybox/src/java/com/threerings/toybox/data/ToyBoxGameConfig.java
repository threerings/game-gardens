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

import java.util.logging.Level;

import com.threerings.util.StreamableHashMap;

import com.threerings.parlor.client.GameConfigurator;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.GameConfig;

import static com.threerings.toybox.Log.log;

/**
 * Provides a mechanism for communicating game configuration information
 * to ToyBox games in a way that avoids having to load any game code
 * during the match making process.
 */
public class ToyBoxGameConfig extends GameConfig
    implements TableConfig
{
    /** A zero argument constructor used when unserializing. */
    public ToyBoxGameConfig ()
    {
    }

    /** Constructs a game config based on the supplied game definition. */
    public ToyBoxGameConfig (GameDefinition gamedef)
    {
        _gamedef = gamedef;
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
        // TODO: sort out
        return null;
    }

    // documentation inherited
    public Class getControllerClass ()
    {
        try {
            return Class.forName(_gamedef.controller);
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
    public String[] getDescription ()
    {
        return new String[0];
    }

    // documentation inherited from interface
    public int getMinimumPlayers ()
    {
        return ((TableMatchConfig)_gamedef.match).minSeats;
    }

    // documentation inherited from interface
    public int getMaximumPlayers ()
    {
        return ((TableMatchConfig)_gamedef.match).maxSeats;
    }

    // documentation inherited from interface
    public int getDesiredPlayers ()
    {
        int defval = ((TableMatchConfig)_gamedef.match).startSeats;
        return ((Integer)getParameter("seats", defval)).intValue();
    }

    // documentation inherited from interface
    public void setDesiredPlayers (int desiredPlayers)
    {
        setParameter("seats", desiredPlayers);
    }

    /**
     * Returns the game definition associated with this config instance.
     */
    public GameDefinition getGameDefinition ()
    {
        return _gamedef;
    }

    /**
     * Sets a configuration parameter in this game config.
     */
    public void setParameter (String key, Object value)
    {
        _params.put(key, value);
    }

    /**
     * Looks up a configuration parameter in this game config. If no value
     * has been set, the default value will be returned.
     */
    public Object getParameter (String key, Object defval)
    {
        Object value = _params.get(key);
        return (value == null) ? defval : value;
    }

    /** Our game definition. */
    protected GameDefinition _gamedef;

    /** Our configured parameters. */
    protected StreamableHashMap _params = new StreamableHashMap();
}
