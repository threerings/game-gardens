//
// $Id: ToyBoxService.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides access to ToyBox invocation services.
 */
public interface ToyBoxService extends InvocationService
{
    /**
     * Issues a request for the oid of the lobby associated with the
     * specified game id.
     */
    public void getLobbyOid (Client client, int gameId, ResultListener rl);
}
