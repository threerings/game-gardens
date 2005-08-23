//
// $Id$

package com.threerings.gardens.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import com.threerings.toybox.server.persist.Game;

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

        ArrayList<Game> games = category.equals("") ?
            // load up the metadata for all of our games
            app.getToyBoxRepository().loadGames() :
            // load up the metadata for the games in this category
            app.getToyBoxRepository().loadGames(category);

        // sort our games by name
        Collections.sort(games, new Comparator<Game>() {
            public int compare (Game g1, Game g2) {
                return g1.name.compareTo(g2.name);
            }
        });
        ctx.put("games", games);
        ctx.put("category", category);
    }
}
