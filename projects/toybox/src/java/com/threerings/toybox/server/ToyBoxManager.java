//
// $Id: ToyBoxManager.java,v 1.1 2004/01/20 14:44:40 mdb Exp $

package com.threerings.toybox.server;

import java.util.Properties;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;

import com.threerings.toybox.Log;
import com.threerings.toybox.data.Game;
import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.server.persist.ToyBoxRepository;

/**
 * Manages the repository of games in the ToyBox.
 */
public class ToyBoxManager
    implements ToyBoxCodes
{
    /**
     * Creates the toybox manager and prepares it for operation.
     */
    public ToyBoxManager (Properties config, ConnectionProvider conprov)
        throws PersistenceException
    {
        // initialize our configuration singleton
        ToyBoxConfig.init(config);

        // create our repository
        _toyrepo = new ToyBoxRepository(conprov);
    }

    /**
     * Returns a reference to our repository.
     */
    public ToyBoxRepository getToyBoxRepository ()
    {
        return _toyrepo;
    }

    /** Our persistent repository. */
    protected ToyBoxRepository _toyrepo;
}
