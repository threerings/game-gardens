//
// $Id: ToyBoxProvider.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.server;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Does something extraordinary.
 */
public interface ToyBoxProvider extends InvocationProvider
{
    /**
     * Handles a {@link ToyBoxService#getLobbyOid} request.
     */
    public void getLobbyOid (ClientObject caller, int gameId,
                             InvocationService.ResultListener rl)
        throws InvocationException;
}
