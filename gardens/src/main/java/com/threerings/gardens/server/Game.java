//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server;

import com.threerings.nexus.distrib.NexusObject;

/** Provides basic game interactions. */
public interface Game {

    /** Wires up the game object for this game. This should be called in the constructor of your
     * game manager. */
    void setObject (NexusObject gameObj);

    /** Informs the Game Gardens server that the game has ended. This will record win/loss
     * information for the players of the game.
     *
     * @param winData an array of values ranging from 0 to 1 indicating the degree to which each
     * player "won" the game. In a traditional game like chess, this would be 0 for the loser, 1
     * for the winner, or 0.5 for each player in the case of a draw. Games with more complex win
     * conditions can choose to provide other values if they make sense. These values are used to
     * update a FIDE-style rating for each player in the game in question.
     */
    void endGame (float[] winData);
}
