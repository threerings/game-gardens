//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.parlor.game.GameConfig;

import com.threerings.toybox.lobby.client.LobbyController;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.toybox.xml.GameDefinition;

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
    public LobbyConfig (GameDefinition gameDefinition)
    {
        _gameDefinition = gameDefinition;
    }

    // documentation inherited
    public Class getControllerClass ()
    {
        return LobbyController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.toybox.lobby.server.LobbyManager";
    }

    /**
     * Derived classes override this function and create the appropriate
     * matchmaking user interface component.
     */
    public JComponent createMatchMakingView (ToyBoxContext ctx)
    {
        return new JLabel("Match-making view goes here.");
    }

    /**
     * Instantiates and returns a game config instance using the game
     * config class name provided when this configuration was constructed.
     *
     * @exception Exception thrown if a problem occurs loading or
     * instantiating the class.
     */
    public GameConfig getGameConfig ()
        throws Exception
    {
        return (GameConfig)Class.forName(_gameDefinition.config).newInstance();
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        if (buf.length() > 1) {
            buf.append(", ");
        }
        buf.append("game_def=").append(_gameDefinition);
    }

    /** The definition for the game we'll be matchmaking. */
    protected GameDefinition _gameDefinition;
}
