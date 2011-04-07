//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.reversi;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

import com.threerings.groovy.data.GroovyGameObject;

/**
 * Defines the state of a Reversi game.
 */
public class ReversiObject extends GroovyGameObject
{
    /** The size of the reversi board along one side. */
    public static final int BOARD_SIZE = 8;

    /** The username of the current turn holder. */
    public Name turnHolder;

    /** The pieces on the board. */
    public DSet /*<ReversiTile>*/ pieces = new DSet();
}
