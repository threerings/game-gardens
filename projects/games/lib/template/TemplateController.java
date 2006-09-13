//
// $Id$

package @package@;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Manages the client side mechanics of the game.
 */
public class @classpre@Controller extends GameController
{
    /**
     * Requests that we leave the game and return to the lobby.
     */
    public void backToLobby ()
    {
        _ctx.getLocationDirector().moveBack();
    }

    @Override // from PlaceController
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        _panel = new @classpre@Panel((ToyBoxContext)ctx, this);
        return _panel;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _gameobj = (@classpre@Object)plobj;
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        // clear out our game object reference
        _gameobj = null;
    }

    // documentation inherited
    protected void gameDidStart ()
    {
        super.gameDidStart();

        // here we can set up anything that should happen at the start of the
        // game
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // here we can clear out anything that needs to be cleared out at the
        // end of a game
    }

    /** Our game panel. */
    protected @classpre@Panel _panel;

    /** Our game distributed object. */
    protected @classpre@Object _gameobj;
}
