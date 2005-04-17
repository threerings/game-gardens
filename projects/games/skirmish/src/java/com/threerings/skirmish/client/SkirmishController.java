//
// $Id: SkirmishController.java,v 1.7 2002/07/12 04:27:03 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.event.ActionEvent;

import com.samskivert.util.Interval;
import com.samskivert.util.ListUtil;
import com.samskivert.util.ObserverList;
import com.threerings.util.RandomUtil;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.skirmish.Log;
import com.threerings.skirmish.data.SkirmishAction;
import com.threerings.skirmish.data.SkirmishObject;

/**
 * Manages the client side mechanics of a Skirmish game.
 */
public class SkirmishController extends GameController
{
    /** The name of the command posted by the "Back to lobby" button in
     * the side bar. */
    public static final String BACK_TO_LOBBY = "back_to_lobby";

    public static interface Tickable
    {
        public void tick ();
    }

    public void registerTickable (Tickable tickable)
    {
        if (!_tickers.contains(tickable)) {
            _tickers.add(tickable);
        }
    }

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // get a handle on our body object
        _self = (BodyObject)_ctx.getClient().getClientObject();
    }

    // documentation inherited
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        _panel = new SkirmishPanel(
            (ToyBoxContext)ctx, (ToyBoxGameConfig)_config, this);
        return _panel;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _skobj = (SkirmishObject)plobj;

        // find out what our player index is
        _selfIndex = ListUtil.indexOf(_skobj.players, _self.username);

        // start the game ticker if we're already in play
        if (_skobj.state == SkirmishObject.IN_PLAY) {
            startTicker();
        }
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        endTicker();
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        if (action.getActionCommand().equals(BACK_TO_LOBBY)) {
            // bail on out
            _ctx.getLocationDirector().moveBack();

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    protected void gameDidStart ()
    {
        super.gameDidStart();
        startTicker();
    }

    protected void gameDidEnd ()
    {
        super.gameDidEnd();
        endTicker();
    }

    protected void startTicker ()
    {
        // start the tick interval
        _ticker = new Interval(_ctx.getClient().getRunQueue()) {
            public void expired () {
                tickTickables();
            }
        };
        _ticker.schedule(100L, true);
    }

    protected void endTicker ()
    {
        if (_ticker != null) {
            _ticker.cancel();
        }
    }

    /**
     * Called ten times per second while the game is in play; ticks all
     * registered tickables.
     */
    protected void tickTickables ()
    {
        _tickers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object obs) {
                ((Tickable)obs).tick();
                return true;
            }
        });
    }

    /** A reference to our game panel. */
    protected SkirmishPanel _panel;

    /** A reference to our game panel. */
    protected SkirmishObject _skobj;

    /** A reference to our body object. */
    protected BodyObject _self;

    /** Our player index or -1 if we're not a player. */
    protected int _selfIndex;

    /** Used to track our tick interval. */
    protected Interval _ticker;

    /** We tick these guys once a second during play. */
    protected ObserverList _tickers =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);
}
