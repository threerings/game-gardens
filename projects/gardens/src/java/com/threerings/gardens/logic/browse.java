//
// $Id$

package com.threerings.gardens.logic;

import com.samskivert.servlet.user.User;
import com.samskivert.velocity.InvocationContext;

import com.threerings.gardens.Log;
import com.threerings.gardens.GardensApp;

/**
 * Displays a list of all registered games.
 */
public class browse extends OptionalUserLogic
{
    // documentation inherited
    public void invoke (InvocationContext ctx, GardensApp app, User user)
        throws Exception
    {
        // load up the metadata for all of our games
        ctx.put("games", app.getToyBoxRepository().loadGames());
    }
}
