//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.groovy.server;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.parlor.game.server.GameManager;

import com.threerings.groovy.data.GroovyGameObject;

/**
 * Extends the standard game manager and wires up various magic bits for
 * Groovy.
 */
public class GroovyGameManager extends GameManager
{
    // @Override // from GameManager
    protected PlaceObject createPlaceObject ()
    {
        return new GroovyGameObject();
    }
}
