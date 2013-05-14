//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server;

/** Provides access to info on the players of the game. */
public interface Players {

    /** Returns the ids of the players in the game. If this is a party game, or some other
     * non-table-game, this method will return a zero-length array. In those games, whether or not
     * someone is a player tends to depend more on whether they participated during a particular
     * round. */
    int[] players ();

    /** Returns the name of the user identified by {@code id}. */
    String name (int id);

    /** Returns the id of the user making the current service request. Note: the user may or may
     * not be a player as defined by {@link #players}. If someone is watching they game, their
     * client can make a service request (due to hacking, or a bug, or maybe by design). */
    int callerId ();
}
