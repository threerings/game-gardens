//
// $Id: ToyBoxDispatcher.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ResultListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.toybox.client.ToyBoxService;
import com.threerings.toybox.data.ToyBoxMarshaller;

/**
 * Dispatches requests to the {@link ToyBoxProvider}.
 */
public class ToyBoxDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public ToyBoxDispatcher (ToyBoxProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new ToyBoxMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case ToyBoxMarshaller.GET_LOBBY_OID:
            ((ToyBoxProvider)provider).getLobbyOid(
                source,
                ((Integer)args[0]).intValue(), (ResultListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
