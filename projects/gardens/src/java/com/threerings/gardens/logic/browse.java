//
// $Id$

package com.threerings.gardens.logic;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.gardens.Log;
import com.threerings.gardens.GardensApp;

/**
 * Displays a list of all registered games.
 */
public class browse implements Logic
{
    // documentation inherited
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        GardensApp gtapp = (GardensApp)app;

        // load up the metadata for all of our games
        ctx.put("games", gtapp.getToyBoxRepository().loadGames());
    }
}
