//
// $Id: ToyBoxMarshaller.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ResultListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.toybox.client.ToyBoxService;

/**
 * Provides the implementation of the {@link ToyBoxService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ToyBoxMarshaller extends InvocationMarshaller
    implements ToyBoxService
{
    /** The method id used to dispatch {@link #getLobbyOid} requests. */
    public static final int GET_LOBBY_OID = 1;

    // documentation inherited from interface
    public void getLobbyOid (Client arg1, int arg2, ResultListener arg3)
    {
        ResultMarshaller listener3 = new ResultMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_LOBBY_OID, new Object[] {
            new Integer(arg2), listener3
        });
    }

}
