//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.server;

import javax.annotation.Generated;

import com.samskivert.sagashi.data.SagashiMarshaller;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link SagashiProvider}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SagashiService.java.")
public class SagashiDispatcher extends InvocationDispatcher<SagashiMarshaller>
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public SagashiDispatcher (SagashiProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public SagashiMarshaller createMarshaller ()
    {
        return new SagashiMarshaller();
    }

    @Override
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case SagashiMarshaller.SUBMIT_WORD:
            ((SagashiProvider)provider).submitWord(
                source, (String)args[0], (InvocationService.ResultListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
