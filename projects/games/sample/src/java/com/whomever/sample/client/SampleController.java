//
// $Id: SampleController.java,v 1.7 2002/07/12 04:27:03 mdb Exp $

package com.whomever.sample.client;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.GameController;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.whomever.sample.data.SampleObject;

/**
 * Manages the client side mechanics of a Sample game.
 */
public class SampleController extends GameController
{
    /** The name of the command posted by the "Back to lobby" button in
     * the side bar. */
    public static final String BACK_TO_LOBBY = "BackToLobby";

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // cast our context
        _ctx = (ToyBoxContext)super._ctx;
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        _panel = new SamplePanel(_ctx, (ToyBoxGameConfig)_config, this);
        return _panel;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // this method is called when we enter the game "room"

        // get a casted reference to our game object
        _sampobj = (SampleObject)plobj;
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        // this method is called when we leave the game "room"
    }

    /**
     * This method is called automatically by the controller system when
     * the player clicks the button that was configured with the {@link
     * #BACK_TO_LOBBY} action and the special {@link #DISPATCHER} which
     * does the necessary reflection magic.
     */
    public void handleBackToLobby (Object source)
    {
        _ctx.getLocationDirector().moveBack();
    }

    // documentation inherited
    protected void gameDidStart ()
    {
        super.gameDidStart();

        // here we can set up anything that should happen at the start of
        // the game
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // here we can clear out anything that needs to be cleared out at
        // the end of a game
    }

    /** Provides access to various client services. */
    protected ToyBoxContext _ctx;

    /** Our game panel. */
    protected SamplePanel _panel;

    /** Our game distributed object. */
    protected SampleObject _sampobj;
}
