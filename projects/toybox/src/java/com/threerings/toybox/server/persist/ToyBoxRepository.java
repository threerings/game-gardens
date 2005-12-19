//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.gamegardens.com/code/
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

package com.threerings.toybox.server.persist;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.Cursor;
import com.samskivert.jdbc.jora.Session;
import com.samskivert.jdbc.jora.Table;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;

import static com.threerings.toybox.Log.log;

/**
 * Provides an interface to our persistent repository of game metadata.
 */
public class ToyBoxRepository extends JORARepository
{
    /**
     * The database identifier used when establishing a database
     * connection. This value being <code>gamedb</code>.
     */
    public static final String GAME_DB_IDENT = "gamedb";

    /**
     * Constructs a new repository with the specified connection provider.
     *
     * @param conprov the connection provider via which we will obtain our
     * database connection.
     */
    public ToyBoxRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, GAME_DB_IDENT);
    }

    /**
     * Loads up all of the games in the repository.
     */
    public ArrayList<Game> loadGames ()
        throws PersistenceException
    {
        return loadGamesBy("", "");
    }

    /**
     * Loads up all of the games in the repository with the specified
     * category.
     */
    public ArrayList<Game> loadGames (String category)
        throws PersistenceException
    {
        category = StringUtil.replace(category, "'", "\\'");
        return loadGamesBy("where category = '" + category + "'", "");
    }

    /**
     * Loads the specified number of the most popular games as determined
     * by the number of minutes played in those games in the current and
     * previous week.
     */
    public ArrayList<Game> loadPopularGames (final int count)
        throws PersistenceException
    {
        final Date thisWeek = getWeek(0);
        final Date lastWeek = getWeek(-1);
        return (ArrayList<Game>)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                // first obtain the game ids of the top N most frequently
                // played games
                String query = "select GAME_ID, SUM(PLAYTIME) as MINUTES " +
                    "from PLAYTIME where PERIOD = '" + thisWeek + "' " +
                    "or PERIOD = '" + lastWeek + "' group by GAME_ID " +
                    "order by MINUTES DESC limit " + count;
                int[] gameIds = new int[count];
                StringBuffer buf = new StringBuffer();
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    for (int ii = 0; rs.next(); ii++) {
                        gameIds[ii] = rs.getInt(1);
                        if (buf.length() > 0) {
                            buf.append(",");
                        }
                        buf.append(gameIds[ii]);
                    }
                } finally {
                    JDBCUtil.close(stmt);
                }

                // next load those games from the database
                Game game = null;
                HashMap<Integer,Game> games = new HashMap<Integer,Game>();
                if (buf.length() > 0) {
                    Cursor c = _gtable.select("where GAME_ID in (" + buf + ")");
                    while ((game = (Game)c.next()) != null) {
                        games.put(game.gameId, game);
                    }
                }

                // finally arrange them into a list in the proper order
                ArrayList<Game> glist = new ArrayList<Game>();
                for (int ii = 0; ii < gameIds.length; ii++) {
                    game = games.get(gameIds[ii]);
                    if (game != null) {
                        glist.add(game);
                    }
                }
                return glist;
            }
        });
    }

    /**
     * Loads the specified number of the most recently created games in
     * the system.
     */
    public ArrayList<Game> loadRecentlyAdded (final int count)
        throws PersistenceException
    {
        return loadGamesBy("", "order by CREATED DESC limit " + count);
    }

    /**
     * Loads the specified number of the most recently updated games in
     * the system.
     */
    public ArrayList<Game> loadRecentlyUpdated (final int count)
        throws PersistenceException
    {
        return loadGamesBy("", "order by LAST_UPDATED DESC limit " + count);
    }

    /**
     * Loads information on a single game from the repository. Returns
     * null if no game exists with the specifed id.
     */
    public Game loadGame (int gameId)
        throws PersistenceException
    {
        return loadGameBy("where GAME_ID = " + gameId);
    }

    /**
     * Loads information on a single game from the repository. Returns
     * null if no game exists with the specifed id.
     */
    public Game loadGame (String gameIdent)
        throws PersistenceException
    {
        gameIdent = gameIdent.replaceAll("[^a-zA-Z]*", "");
        return loadGameBy("where IDENT = '" + gameIdent + "'");
    }

    /**
     * Inserts the supplied game into the repository. {@link Game#gameId}
     * will be filled in by this method with the game's newly assigned
     * unique identifier.
     */
    public void insertGame (final Game game)
        throws PersistenceException
    {
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                _gtable.insert(game);
                game.gameId = liaison.lastInsertedId(conn);
                return null;
            }
        });
    }

    /**
     * Updates the supplied game in the repository. Returns true if a
     * matching row was found and updated, false if no rows matched.
     */
    public boolean updateGame (final Game game)
        throws PersistenceException
    {
        Boolean rv = (Boolean)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                int mod = _gtable.update(game);
                switch (mod) {
                case 0: return Boolean.FALSE;
                case 1: return Boolean.TRUE;
                default:
                    log.warning("updateGame() modified more than one row?! " +
                                "[game=" + game + ", modified=" + mod + "].");
                    return Boolean.TRUE; // something was updated!
                }
            }
        });
        return rv.booleanValue();
    }

    /**
     * Increments the number of minutes played for the specified game in
     * the current week.
     */
    public void incrementPlaytime (int gameId, int minutes)
        throws PersistenceException
    {
        Date when = getWeek(0);
        final String uquery = "update PLAYTIME " +
            "set PLAYTIME = PLAYTIME + " + minutes + " " +
            "where GAME_ID = " + gameId + " and PERIOD = '" + when + "'";
        final String iquery = "insert into PLAYTIME " +
            "(GAME_ID, PERIOD, PLAYTIME) " +
            "values(" + gameId + ", '" + when + "', " + minutes + ")";
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    // first try updating
                    stmt = conn.createStatement();
                    if (stmt.executeUpdate(uquery) == 0) {
                        // if that failed to modify anything, insert
                        stmt.executeUpdate(iquery);
                    }

                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    /**
     * Helper function for {@link #loadGames()} and {@link
     * #loadGames(String)}.
     *
     * @param where the where clause to use in our query (or "").
     * @param extra any extra "order by" or "limit" clauses to append to
     * the query, or "" if none are needed.
     */
    protected ArrayList<Game> loadGamesBy (String where, String extra)
        throws PersistenceException
    {
        where = StringUtil.isBlank(where) ? "where " : (where + " and ");
        where = where + "STATUS = '" + Game.Status.READY.toString() + "'";
        final String query = where + " " + extra;
        return (ArrayList<Game>)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return _gtable.select(query).toArrayList();
            }
        });
    }

    /** Helper function for {@link #loadGame(int)} and {@link
     * #loadGame(String)}. */
    protected Game loadGameBy (final String query)
        throws PersistenceException
    {
        return (Game)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return (Game)_gtable.select(query).next();
            }
        });
    }

    /** Returns a {@link Date} instance configured to the beginning of the
     * current (or current + offset) week. */
    protected static Date getWeek (int offset)
    {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int week = now.get(Calendar.WEEK_OF_YEAR);
        now.clear();
        now.set(Calendar.YEAR, year);
        now.set(Calendar.WEEK_OF_YEAR, week);
        now.add(Calendar.WEEK_OF_YEAR, offset);
        return new Date(now.getTimeInMillis());
    }

    // documentation inherited
    protected void createTables (Session session)
    {
	_gtable = new Table(Game.class.getName(), "GAMES",
                            session, "GAME_ID", true);
    }

    /** A wrapper that provides access to the games table. */
    protected Table _gtable;
}
