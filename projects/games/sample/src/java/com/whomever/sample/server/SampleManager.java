//
// $Id: SampleManager.java,v 1.21 2004/08/27 18:51:26 mdb Exp $

package com.whomever.sample.server;

import com.threerings.parlor.game.GameManager;
import com.threerings.toybox.data.ToyBoxGameConfig;

import com.whomever.sample.data.SampleBoard;
import com.whomever.sample.data.SampleCodes;
import com.whomever.sample.data.SampleObject;

/**
 * Handles the server side of our sample game.
 */
public class SampleManager extends GameManager
    implements SampleCodes
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return SampleObject.class;
    }

    // documentation inherited
    public void didInit ()
    {
        super.didInit();

        // this is called when our manager is created but before any
        // game-specific actions take place; we don't yet have our game
        // object at this point but we do have our game configuration

        // get a casted reference to our game configuration
        _sampconf = (ToyBoxGameConfig)_config;
    }

    // documentation inherited
    public void didStartup ()
    {
        super.didStartup();

        // this method is called after we have created our game object but
        // before we do any game related things

        // grab our own casted game object reference
        _sampobj = (SampleObject)_gameobj;
    }

    // documentation inherited
    public void didShutdown ()
    {
        super.didShutdown();

        // this is called right before we finally disappear for good
    }

    // documentation inherited
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // when all the players have entered the game room, the game is
        // automatically started and this method is called just before the
        // event is delivered to the clients that will start the game

        // this is the place to do any pre-game setup that needs to be
        // done each time a game is started rather than just once at the
        // very beginning (those sorts of things should be done in
        // didStartup())

        // generate the game board
        int size = (Integer)_sampconf.params.get("board_size");
        _sampobj.setBoard(new SampleBoard(size, size));
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // this is called after the game has ended. somewhere in the game
        // manager a call to endGame() should be made when the manager
        // knows the game to be over and that will trigger the end-of-game
        // processing including calling this method
    }

    /** A casted reference to our game object. */
    protected SampleObject _sampobj;

    /** Our game configuration object. */
    protected ToyBoxGameConfig _sampconf;
}
