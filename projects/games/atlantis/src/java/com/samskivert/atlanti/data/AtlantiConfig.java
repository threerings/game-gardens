//
// $Id: AtlantiConfig.java,v 1.6 2004/12/09 23:41:59 mdb Exp $

package com.samskivert.atlanti.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.parlor.client.GameConfigurator;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.GameConfig;

import com.samskivert.atlanti.client.AtlantiConfigurator;
import com.samskivert.atlanti.client.AtlantiController;

/**
 * Describes the configuration parameters for the game.
 */
public class AtlantiConfig extends GameConfig
    implements TableConfig
{
    // documentation inherited
    public String getGameName ()
    {
        return "atlanti";
    }

    // documentation inherited
    public String getBundleName ()
    {
        return "atlanti";
    }

    // documentation inherited
    public byte getRatingTypeId ()
    {
        return (byte)-1;
    }

    // documentation inherited
    public Class getControllerClass ()
    {
        return AtlantiController.class;
    }

    // documentation inherited from interface
    public GameConfigurator createConfigurator ()
    {
        return new AtlantiConfigurator();
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.samskivert.atlanti.server.AtlantiManager";
    }

    // documentation inherited
    public int getMinimumPlayers ()
    {
        return 2;
    }

    // documentation inherited
    public int getMaximumPlayers ()
    {
        return 5;
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

    // documentation inherited
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(_desiredPlayers);
    }

    // documentation inherited
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
