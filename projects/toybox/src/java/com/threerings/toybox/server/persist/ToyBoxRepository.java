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
import java.util.List;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.Cursor;
import com.samskivert.jdbc.jora.Table;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.toybox.server.ToyBoxManager;

import static com.threerings.toybox.Log.log;

/**
 * Provides an interface to our persistent repository of game metadata.
 */
public class ToyBoxRepository extends JORARepository
    implements ToyBoxManager.GameRepository
{
    /**
     * The database identifier used when establishing a database connection. This value being
     * <code>gamedb</code>.
     */
    public static final String GAME_DB_IDENT = "gamedb";

    /**
     * Constructs a new repository with the specified connection provider.
     *
     * @param conprov the connection provider via which we will obtain our database connection.
     */
    public ToyBoxRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, GAME_DB_IDENT);
    }

    /**
     * Loads up all of the games in the repository.
     */
    public ArrayList<GameRecord> loadGames ()
        throws PersistenceException
    {
        return loadGamesBy("", "");
    }

    /**
     * Loads up all of the games in the repository with the specified category.
     */
    public ArrayList<GameRecord> loadGames (String category)
        throws PersistenceException
    {
        category = StringUtil.replace(category, "'", "\\'");
        return loadGamesBy("where category = '" + category + "'", "");
    }

    /**
     * Loads the specified number of the most popular games as determined by the number of minutes
     * played in those games in the current and previous week.
     */
    public ArrayList<GameRecord> loadPopularGames (final int count)
        throws PersistenceException
    {
        final Date thisWeek = getWeek(0);
        final Date lastWeek = getWeek(-1);
        return execute(new Operation<ArrayList<GameRecord>>() {
            public ArrayList<GameRecord> invoke (Connection conn, DatabaseLiaison liaison)
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
                GameRecord game = null;
                HashMap<Integer,GameRecord> games = new HashMap<Integer,GameRecord>();
                if (buf.length() > 0) {
                    Cursor c = _gtable.select(
                        conn, "where GAME_ID in (" + buf + ")");
                    while ((game = (GameRecord)c.next()) != null) {
                        games.put(game.gameId, game);
                    }
                }

                // finally arrange them into a list in the proper order
                ArrayList<GameRecord> glist = new ArrayList<GameRecord>();
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
     * Loads the specified number of the most recently created games in the system.
     */
    public ArrayList<GameRecord> loadRecentlyAdded (final int count)
        throws PersistenceException
    {
        return loadGamesBy("", "order by CREATED DESC limit " + count);
    }

    /**
     * Loads the specified number of the most recently updated games in the system.
     */
    public ArrayList<GameRecord> loadRecentlyUpdated (final int count)
        throws PersistenceException
    {
        return loadGamesBy("", "order by LAST_UPDATED DESC limit " + count);
    }

    /**
     * Loads information on a single game from the repository. Returns null if no game exists with
     * the specifed id.
     */
    public GameRecord loadGame (int gameId)
        throws PersistenceException
    {
        return loadGameBy("where GAME_ID = " + gameId);
    }

    /**
     * Loads information on a single game from the repository. Returns null if no game exists with
     * the specifed id.
     */
    public GameRecord loadGame (String gameIdent)
        throws PersistenceException
    {
        gameIdent = gameIdent.replaceAll("[^a-zA-Z]*", "");
        return loadGameBy("where IDENT = '" + gameIdent + "'");
    }

    /**
     * Inserts the supplied game into the repository. {@link GameRecord#gameId} will be filled in
     * by this method with the game's newly assigned unique identifier.
     */
    public void insertGame (final GameRecord game)
        throws PersistenceException
    {
        game.gameId = insert(_gtable, game);
    }

    /**
     * Updates the supplied game in the repository. Returns true if a matching row was found and
     * updated, false if no rows matched.
     */
    public boolean updateGame (final GameRecord game)
        throws PersistenceException
    {
        int mod = update(_gtable, game);
        switch (mod) {
        case 0: return false;
        case 1: return true;
        default:
            log.warning("updateGame() modified more than one row?! " +
                        "[game=" + game + ", modified=" + mod + "].");
            return true; // something was updated!
        }
    }

    /**
     * Increments the number of minutes played for the specified game in the current week.
     */
    public void incrementPlaytime (int gameId, int minutes)
        throws PersistenceException
    {
        Date when = getWeek(0);
        // first try updating
        if (update("update PLAYTIME set PLAYTIME = PLAYTIME + " + minutes +
                   " where GAME_ID = " + gameId + " and PERIOD = '" + when + "'") == 0) {
            // if that failed to modify anything, insert
            update("insert into PLAYTIME (GAME_ID, PERIOD, PLAYTIME) " +
                   "values(" + gameId + ", '" + when + "', " + minutes + ")");
        }
    }

    /**
     * Clears out the players online counts. Called when the server starts up.
     */
    public void clearOnlineCounts ()
        throws PersistenceException
    {
        update("delete from " + _otable.getName());
    }

    /**
     * Updates the number of players online for the specified game.
     */
    public void updateOnlineCount (int gameId, int players)
        throws PersistenceException
    {
        OnlineRecord record = new OnlineRecord();
        record.gameId = gameId;
        record.players = players;
        store(_otable, record);
    }

    /**
     * Looks up the number of players online for the specified game.
     */
    public int getOnlineCount (int gameId)
        throws PersistenceException
    {
        OnlineRecord record = load(_otable, "where GAME_ID = " + gameId);
        return (record == null) ? 0 : record.players;
    }

    /**
     * Returns information on how many players are in games.
     */
    public List<FullOnlineRecord> getOnlineCounts ()
        throws PersistenceException
    {
        return execute(new Operation<List<FullOnlineRecord>>() {
            public List<FullOnlineRecord> invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                // first obtain the game ids of the top N most frequently played games
                String query = "select ONLINE.GAME_ID, PLAYERS, NAME from ONLINE, GAMES " +
                    "where PLAYERS > 0 && ONLINE.GAME_ID = GAMES.GAME_ID";
                ArrayList<FullOnlineRecord> results = new ArrayList<FullOnlineRecord>();
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    for (int ii = 0; rs.next(); ii++) {
                        FullOnlineRecord record = new FullOnlineRecord();
                        record.gameId = rs.getInt(1);
                        record.players = rs.getInt(2);
                        record.name = rs.getString(3);
                        results.add(record);
                    }
                    return results;

                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Helper function for {@link #loadGames()} and {@link #loadGames(String)}.
     *
     * @param where the where clause to use in our query (or "").
     * @param extra any extra "order by" or "limit" clauses to append to the query, or "" if none
     * are needed.
     */
    protected ArrayList<GameRecord> loadGamesBy (String where, String extra)
        throws PersistenceException
    {
        where = StringUtil.isBlank(where) ? "where " : (where + " and ");
        where = where + "STATUS = '" + GameRecord.Status.READY.toString() + "'";
        return loadAll(_gtable, where + " " + extra);
    }

    /** Helper function for {@link #loadGame(int)} and {@link #loadGame(String)}. */
    protected GameRecord loadGameBy (final String query)
        throws PersistenceException
    {
        return load(_gtable, query);
    }

    /** Returns a {@link Date} instance configured to the beginning of the current (or current +
     * offset) week. */
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
    protected void createTables ()
    {
	_gtable = new Table<GameRecord>(GameRecord.class, "GAMES", "GAME_ID", true);
	_otable = new Table<OnlineRecord>(OnlineRecord.class, "ONLINE", "GAME_ID", true);
    }

    @Override // documentation inherited
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        super.migrateSchema(conn, liaison);

        JDBCUtil.createTableIfMissing(conn, "GAMES", new String[] {
            "GAME_ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY",
            "CATEGORY VARCHAR(255) NOT NULL",
            "NAME VARCHAR(255) NOT NULL",
            "MAINTAINER_ID INTEGER NOT NULL",
            "STATUS VARCHAR(255) NOT NULL",
            "HOST VARCHAR(255) NOT NULL",
            "DEFINITION TEXT NOT NULL",
            "DIGEST VARCHAR(255) NOT NULL",
            "DESCRIPTION TEXT NOT NULL",
            "INSTRUCTIONS TEXT NOT NULL",
            "CREDITS TEXT NOT NULL",
            "CREATED DATE NOT NULL",
            "LAST_UPDATED DATE NOT NULL",
            "KEY (MAINTAINER_ID)",
        }, "");

        JDBCUtil.createTableIfMissing(conn, "ONLINE", new String[] {
            "GAME_ID INTEGER NOT NULL",
            "PLAYERS INTEGER NOT NULL",
        }, "");

        JDBCUtil.createTableIfMissing(conn, "PLAYTIME", new String[] {
            "GAME_ID INTEGER NOT NULL",
            "PERIOD DATE NOT NULL",
            "PLAYTIME INTEGER NOT NULL",
            "PRIMARY KEY (GAME_ID, PERIOD)",
        }, "");
    }

    /** A wrapper that provides access to the games table. */
    protected Table<GameRecord> _gtable;

    /** Provides access to the players online table. */
    protected Table<OnlineRecord> _otable;
}
