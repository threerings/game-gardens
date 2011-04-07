//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.data;

import javax.annotation.Generated;

import com.samskivert.sagashi.client.SagashiService;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link SagashiService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SagashiService.java.")
public class SagashiMarshaller extends InvocationMarshaller
    implements SagashiService
{
    /** The method id used to dispatch {@link #submitWord} requests. */
    public static final int SUBMIT_WORD = 1;

    // from interface SagashiService
    public void submitWord (Client arg1, String arg2, InvocationService.ResultListener arg3)
    {
        InvocationMarshaller.ResultMarshaller listener3 = new InvocationMarshaller.ResultMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, SUBMIT_WORD, new Object[] {
            arg2, listener3
        });
    }
}
