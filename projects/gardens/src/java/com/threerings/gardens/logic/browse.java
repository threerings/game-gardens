//
// $Id$

package com.threerings.gardens.logic;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.ParameterUtil;
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
        String category = ParameterUtil.getParameter(
            ctx.getRequest(), "category", false);

        if (category.equals("")) {
            // load up the metadata for all of our games
            ctx.put("games", app.getToyBoxRepository().loadGames());

        } else {
            // load up the metadata for the games in this category
            ctx.put("category", category);
            ctx.put("games", app.getToyBoxRepository().loadGames(category));
        }
    }
}
