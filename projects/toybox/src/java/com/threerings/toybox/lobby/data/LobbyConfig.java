//
// $Id: LobbyConfig.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.lobby.data;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.parlor.game.GameConfig;

import com.threerings.toybox.lobby.client.LobbyController;
import com.threerings.toybox.util.ToyBoxContext;

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
    public LobbyConfig (String gameConfigClass)
    {
        _gameConfigClass = gameConfigClass;
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
        return (GameConfig)Class.forName(_gameConfigClass).newInstance();
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        if (buf.length() > 1) {
            buf.append(", ");
        }
        buf.append("game_config=").append(_gameConfigClass);
    }

    /** The name of the game config class that represents the type of game
     * we are matchmaking for in this lobby. */
    protected String _gameConfigClass;
}
