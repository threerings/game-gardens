//
// $Id: ToyBoxManager.java,v 1.2 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.server;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.toybox.Log;
import com.threerings.toybox.data.Game;
import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.server.persist.ToyBoxRepository;

/**
 * Manages the server side of the ToyBox services.
 */
public class ToyBoxManager
    implements ToyBoxCodes, ToyBoxProvider
{
    /**
     * Prepares the toybox manager for operation.
     */
    public void init (InvocationManager invmgr, ConnectionProvider conprov)
        throws PersistenceException
    {
        // create our repository
        _toyrepo = new ToyBoxRepository(conprov);

        // register ourselves as providing the toybox service
        invmgr.registerDispatcher(new ToyBoxDispatcher(this), true);
    }

    /**
     * Returns a reference to our repository.
     */
    public ToyBoxRepository getToyBoxRepository ()
    {
        return _toyrepo;
    }

    // documentation inherited from interface
    public void getLobbyOid (ClientObject caller, int gameId,
                             InvocationService.ResultListener rl)
        throws InvocationException
    {
        // TODO
    }

    /** Our persistent repository. */
    protected ToyBoxRepository _toyrepo;
}
