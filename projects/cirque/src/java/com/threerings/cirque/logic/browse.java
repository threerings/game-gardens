//
// $Id$

package com.threerings.cirque.logic;

import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.threerings.cirque.Log;
import com.threerings.cirque.CirqueDeJeuApp;

/**
 * Displays a list of all registered games.
 */
public class browse implements Logic
{
    // documentation inherited
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        CirqueDeJeuApp gtapp = (CirqueDeJeuApp)app;

        // load up the metadata for all of our games
        ctx.put("games", gtapp.getToyBoxRepository().loadGames());
    }
}
