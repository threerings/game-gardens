//
// $Id: SkirmishConfig.java,v 1.12 2002/07/26 21:53:22 mdb Exp $

package com.threerings.skirmish.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.parlor.client.GameConfigurator;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.GameConfig;

import com.threerings.skirmish.client.SkirmishConfigurator;
import com.threerings.skirmish.client.SkirmishController;

/**
 * Describes the configuration parameters for a game of Skirmish.
 */
public class SkirmishConfig extends GameConfig
    implements TableConfig
{
    /** The width of the board to be used in play. */
    public int boardWidth = 32;

    /** The height of the board to be used in play. */
    public int boardHeight = 32;

    /** The size of the hands to be used by each player. */
    public int handSize = 5;

    /** The number of seconds allowed for each turn. */
    public int turnInterval = 20;

    /** The number of turns that must elapse without the attacker hitting
     * any vessel before the skirmish is ended. */
    public int escapeDuration = 12;

    /** A damage handicap that will be applied when starting the
     * game. Values range from -9 to 9, negative numbers being applied to
     * the attacker, positive the defender. */
    public int handicap = 0;

    /** The density of board features from zero to ten. */
    public int featureDensity = 5;

    /** The speed with which tokens are created; 5 is normal, 1 super
     * slow, 10 super fast. */
    public int tokenSpeed = 5;

    // documentation inherited
    public String getGameName ()
    {
        return "skirmish";
    }

    // documentation inherited
    public String getBundleName ()
    {
        return "skirmish";
    }

    // documentation inherited
    public Class getControllerClass ()
    {
        return SkirmishController.class;
    }

    // documentation inherited
    public GameConfigurator createConfigurator ()
    {
        return new SkirmishConfigurator();
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.skirmish.server.SkirmishManager";
    }

    // documentation inherited
    public int getMinimumPlayers ()
    {
        return 2;
    }

    // documentation inherited
    public int getMaximumPlayers ()
    {
        return 2;
    }

    // documentation inherited
    public int getDesiredPlayers ()
    {
        return _desiredPlayers;
    }

    // documentation inherited
    public void setDesiredPlayers (int desiredPlayers)
    {
        _desiredPlayers = desiredPlayers;
    }

    // documentation inherited from interface
    public boolean isPrivateTable ()
    {
        return _privateTable;
    }
    
    // documentation inherited from interface
    public void setPrivateTable (boolean privateTable)
    {
        _privateTable = privateTable;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(_desiredPlayers);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _desiredPlayers = in.readInt();
    }

    /** The desired number of players for this game or -1 if there is no
     * specific desired number of players. */
    protected int _desiredPlayers;

    /** Indicates whether or not we're configuring a private table. */
    protected boolean _privateTable;
}
