//
// $Id$

package com.samskivert.sagashi.server;

import com.samskivert.sagashi.client.SagashiService;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link SagashiService}.
 */
public interface SagashiProvider extends InvocationProvider
{
    /**
     * Handles a {@link SagashiService#submitWord} request.
     */
    void submitWord (ClientObject caller, String arg1, InvocationService.ResultListener arg2)
        throws InvocationException;
}
