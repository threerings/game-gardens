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

package com.threerings.gardens.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import com.samskivert.depot.PersistenceContext;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.StringUtil;

import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.persist.GameRecord;
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
            PersistenceContext pctx = new PersistenceContext();
            pctx.init(ToyBoxRepository.GAME_DB_IDENT,
                      new StaticConnectionProvider(ToyBoxConfig.getJDBCConfig()), null);
            ToyBoxRepository toyrepo = new ToyBoxRepository(pctx);
            pctx.initializeRepositories(true);

            String path = docroot + File.separator + "recently_added_incl.html";
            writeGameList(toyrepo.loadRecentlyAdded(9), new File(path));

            path = docroot + File.separator + "recently_updated_incl.html";
            writeGameList(toyrepo.loadRecentlyUpdated(9), new File(path));

            path = docroot + File.separator + "most_popular_incl.html";
            writeGameList(toyrepo.loadPopularGames(9), new File(path));

            path = docroot + File.separator + "all_games_incl.html";
            writeExtGameList(toyrepo.loadGames(), new File(path));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected static void writeGameList (List<GameRecord> games, File target)
    {
        try {
            PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(target)));
            for (Object obj : games) {
                GameRecord game = (GameRecord)obj;
                out.println("<li> <a href=\"/gardens/view_game.wm?gameid=" +
                            game.gameId + "\">" + game.name + "</a>");
            }
            out.close();

        } catch (IOException ioe) {
            System.err.println("Error writing to '" + target + "'.");
            ioe.printStackTrace(System.err);
        }
    }

    protected static void writeExtGameList (List<GameRecord> games, File target)
    {
        // sort them by name
        Collections.sort(games, new Comparator<GameRecord>() {
            public int compare (GameRecord g1, GameRecord g2) {
                return g1.name.compareTo(g2.name);
            }
        });

        try {
            PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(target)));
            for (Object obj : games) {
                GameRecord game = (GameRecord)obj;
                String desc = StringUtil.truncate(game.description, 80, "...");
                String iurl = "http://www.gamegardens.com/gardens/" +
                    "view_game.wm?gameid=" + game.gameId;
                out.println("<li> <a href=\"" + iurl + "\">" +
                            game.name + "</a><br>" + desc);
            }
            out.close();

        } catch (IOException ioe) {
            System.err.println("Error writing to '" + target + "'.");
            ioe.printStackTrace(System.err);
        }
    }
}
