//
// $Id: browse.java,v 1.2 2004/11/15 01:49:34 mdb Exp $

package com.threerings.gametable.logic;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.gametable.Log;
import com.threerings.gametable.GameTableApp;

/**
 * Displays a list of all registered games.
 */
public class browse implements Logic
{
    // documentation inherited
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GameTableApp gtapp = (GameTableApp)app;

        // load up the metadata for all of our games
        ctx.put("games", gtapp.getToyBoxRepository().loadGames());
    }
}
