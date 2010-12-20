//
// $Id$

package com.threerings.toybox.data;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.parlor.game.data.GameConfig;

/**
 * Used to configure the match-making interface for a game. Particular match-making mechanisms
 * extend this class and specify their own special configuration parameters.
 */
public abstract class MatchConfig extends SimpleStreamableObject
{
    /** Returns the matchmaking type to use for this game, e.g. {@link GameConfig#SEATED_GAME}. */
    public abstract int getMatchType ();

    /** Returns the minimum number of players needed to play this game. */
    public abstract int getMinimumPlayers ();

    /** Returns the maximum number of players permitted to play this game. */
    public abstract int getMaximumPlayers ();
}
