//
// $Id$

package com.samskivert.sagashi.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.samskivert.sagashi.client.SagashiService;

/**
 * Defines the server side interface of the {@link SagashiService}.
 */
public interface SagashiProvider extends InvocationProvider
{
    /** Handles a {@link SagashiService#submitWord} request. */
    public void submitWord (ClientObject caller, String word,
                            SagashiService.ResultListener cl)
        throws InvocationException;
}
