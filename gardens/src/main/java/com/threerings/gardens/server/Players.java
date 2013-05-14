//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server;

/** Provides access to info on the players of the game. */
public interface Players {

    /** Returns the number of players in the game. */
    int playerCount ();

    /** Returns the name of the player at {@code index}. */
    String playerName (int index);

    /** Returns the index of the player making the current service request, or -1 if the requester
     * is not a player. */
    int currentPlayerIndex ();
}
