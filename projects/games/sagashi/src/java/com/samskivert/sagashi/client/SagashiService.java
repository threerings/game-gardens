//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides a mechanism for the client to make requests of the server.
 */
public interface SagashiService extends InvocationService
{
    /** Submits a word for scoring during a game. */
    public void submitWord (Client client, String word, ResultListener cl);
}
