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
import java.sql.SQLException;
import java.util.ArrayList;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
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
    public ArrayList loadGames ()
        throws PersistenceException
    {
        return loadGamesWhere("");
    }

    /**
     * Loads up all of the games in the repository with the specified
     * category.
     */
    public ArrayList loadGames (String category)
        throws PersistenceException
    {
        category = StringUtil.replace(category, "'", "\\'");
        return loadGamesWhere("where category = '" + category + "'");
    }

    /** Helper function for the <code>loadGames</code> methods. */
    protected ArrayList loadGamesWhere (final String query)
        throws PersistenceException
    {
        return (ArrayList)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return _gtable.select(query).toArrayList();
            }
        });
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
     * Helper function for the {@link #loadGame(int)} and {@link
     * #loadGame(String)}.
     */
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

    // documentation inherited
    protected void createTables (Session session)
    {
	_gtable = new Table(Game.class.getName(), "GAMES",
                            session, "GAME_ID", true);
    }

    /** A wrapper that provides access to the games table. */
    protected Table _gtable;
}
