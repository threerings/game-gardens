//
// $Id$

package com.threerings.gardens.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.logging.Level;

import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.StringUtil;

import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.Game;
import com.threerings.toybox.server.persist.ToyBoxRepository;

/**
 * Updates our snapshots of the most popular, recently updated and
 * recently created games.
 */
public class UpdateSnapshots
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: UpdateSnapshots docroot");
            System.exit(-1);
        }
        String docroot = args[0];

        try {
            ToyBoxRepository toyrepo =
                new ToyBoxRepository(
                    new StaticConnectionProvider(ToyBoxConfig.getJDBCConfig()));

            String path = docroot + File.separator + "recently_added_incl.html";
            writeGameList(toyrepo.loadRecentlyAdded(9), new File(path));

            path = docroot + File.separator + "recently_updated_incl.html";
            writeGameList(toyrepo.loadRecentlyUpdated(9), new File(path));

            path = docroot + File.separator + "most_popular_incl.html";
            writeGameList(toyrepo.loadPopularGames(9), new File(path));

            path = docroot + File.separator + "many_popular_incl.html";
            writeExtGameList(toyrepo.loadPopularGames(50), new File(path));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected static void writeGameList (ArrayList games, File target)
    {
        try {
            PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(target)));
            for (Object obj : games) {
                Game game = (Game)obj;
                out.println("<li> <a href=\"/gardens/view_game.wm?gameid=" +
                            game.gameId + "\">" + game.name + "</a>");
            }
            out.close();

        } catch (IOException ioe) {
            System.err.println("Error writing to '" + target + "'.");
            ioe.printStackTrace(System.err);
        }
    }

    protected static void writeExtGameList (ArrayList games, File target)
    {
        try {
            PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(target)));
            for (Object obj : games) {
                Game game = (Game)obj;
                String desc = StringUtil.truncate(game.description, 80, "...");
                out.println("<li> <a href=\"http://www.gamegardens.com/" +
                            "gardens/game_" + game.gameId + ".jnlp\">" +
                            game.name + "</a><br>" + desc);
            }
            out.close();

        } catch (IOException ioe) {
            System.err.println("Error writing to '" + target + "'.");
            ioe.printStackTrace(System.err);
        }
    }
}
