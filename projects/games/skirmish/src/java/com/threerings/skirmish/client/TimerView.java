//
// $Id: TimerView.java,v 1.2 2002/07/12 04:27:03 mdb Exp $

package com.threerings.skirmish.client;

import javax.swing.JProgressBar;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.skirmish.data.SkirmishObject;

/**
 * Displays the time remaining in this turn.
 */
public class TimerView extends JProgressBar
    implements PlaceView, SkirmishController.Tickable
{
    public TimerView (
        ParlorContext ctx, SkirmishController ctrl, int turnInterval)
    {
        super(0, 100);

        _ctx = ctx;
        _turnInterval = turnInterval;

        // register ourselves with the skirmish controller
        ctrl.registerTickable(this);
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _skobj = (SkirmishObject)plobj;

        // set up our initial time remaining
        setTimeRemaining();
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        _skobj = null;
    }

    // documentation inherited from interface
    public void tick ()
    {
        setTimeRemaining();
    }

    protected void setTimeRemaining ()
    {
        long now = System.currentTimeMillis();
        long eot = _ctx.getClient().fromServerTime(_skobj.nextTurn);
        int pctdone = (int)(((eot-now) * 100) / (_turnInterval*1000));
        setValue(pctdone);
    }

    /** Provides access to client services. */
    protected ParlorContext _ctx;

    /** The number of seconds per turn. */
    protected int _turnInterval;

    /** Our game object. */
    protected SkirmishObject _skobj;
}
