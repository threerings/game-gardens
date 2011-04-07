//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.server;

import javax.annotation.Generated;

import com.samskivert.sagashi.client.SagashiService;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link SagashiService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SagashiService.java.")
public interface SagashiProvider extends InvocationProvider
{
    /**
     * Handles a {@link SagashiService#submitWord} request.
     */
    void submitWord (ClientObject caller, String arg1, InvocationService.ResultListener arg2)
        throws InvocationException;
}
