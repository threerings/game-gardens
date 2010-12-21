//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/samskivert/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.server;

import com.samskivert.sagashi.data.SagashiMarshaller;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link SagashiProvider}.
 */
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

    @Override // documentation inherited
    public SagashiMarshaller createMarshaller ()
    {
        return new SagashiMarshaller();
    }

    @Override // documentation inherited
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
