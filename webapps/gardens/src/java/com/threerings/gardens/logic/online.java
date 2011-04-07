//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
