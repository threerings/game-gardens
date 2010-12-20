//
// $Id$

package com.threerings.gardens.logic;

import java.util.List;

import com.samskivert.util.Tuple;
import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.toybox.server.persist.OnlineRecord;

import com.threerings.gardens.GardensApp;

/**
 * Provides a listing of all games that have players online right now.
 */
public class online
    implements Logic
{
    // documentation inherited from interface
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GardensApp gtapp = (GardensApp)app;
        long now = System.currentTimeMillis();
        if (_online == null || now - _lastUpdated > UPDATE_INTERVAL) {
            _online = gtapp.getToyBoxRepository().getOnlineCounts();
            _lastUpdated = now;
        }
        ctx.put("online", _online);
    }

    protected List<Tuple<String,OnlineRecord>> _online;
    protected long _lastUpdated;

    protected static final long UPDATE_INTERVAL = 60 * 1000L;
}
