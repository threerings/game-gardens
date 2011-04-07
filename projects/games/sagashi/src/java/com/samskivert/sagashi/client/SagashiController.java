//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.client;

import com.threerings.util.MessageBundle;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.samskivert.sagashi.data.SagashiObject;

/**
 * Manages the client side mechanics of a Sagashi game.
 */
public class SagashiController extends GameController
{
    /** A command that requests that a word be submitted for scoring. */
    public static final String SUBMIT_WORD = "submitWord";

    /** The name of the command posted by the "Back to lobby" button in the
     * side bar. */
    public static final String BACK_TO_LOBBY = "backToLobby";

    /**
     * Requests to submit the specified word.
     */
    public void submitWord (Object source, final String word)
    {
        SagashiService.ResultListener rl = new SagashiService.ResultListener() {
            public void requestProcessed (Object result) {
                String msg = MessageBundle.tcompose(
                    "m.word_accepted", word, String.valueOf(result));
                _panel.displayStatus(msg);
                _panel.recordScore(word, (Integer)result);
            }
            public void requestFailed (String cause) {
                _panel.displayStatus(cause);
            }
        };
        _sagaobj.service.submitWord(_ctx.getClient(), word, rl);
    }

    /**
     * This method is called automatically by the controller system when the
     * player clicks the button that was configured with the {@link
     * #BACK_TO_LOBBY} action.
     */
    public void backToLobby (Object source)
    {
        _ctx.getLocationDirector().moveBack();
    }

    @Override // from GameController
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _sagaobj = (SagashiObject)plobj;
    }

    @Override // from GameController
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        _sagaobj = null;
    }

    @Override // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // cast our context
        _ctx = (ToyBoxContext)super._ctx;
    }

    @Override // documentation inherited
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        _panel = new SagashiPanel(
            (ToyBoxContext)ctx, (ToyBoxGameConfig)_config, this);
        return _panel;
    }

    @Override // documentation inherited
    protected void gameDidStart ()
    {
        super.gameDidStart();
        _panel.gameDidStart();
    }

    @Override // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();
        _panel.displayStatus("m.game_over");
    }

    /** Provides access to various client services. */
    protected ToyBoxContext _ctx;

    /** Our game panel. */
    protected SagashiPanel _panel;

    /** Our game distributed object. */
    protected SagashiObject _sagaobj;
}
