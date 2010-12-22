//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
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

import java.io.StringReader;
import java.sql.Date;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.GeneratedValue;
import com.samskivert.depot.annotation.GenerationType;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.annotation.Index;
import com.samskivert.depot.expression.ColumnExp;
import com.samskivert.util.StringUtil;

import com.threerings.presents.server.InvocationException;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.xml.GameParser;

/**
 * Contains information about a game registration.
 */
@Entity(name="GAMES")
public class GameRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<GameRecord> _R = GameRecord.class;
    public static final ColumnExp GAME_ID = colexp(_R, "gameId");
    public static final ColumnExp CATEGORY = colexp(_R, "category");
    public static final ColumnExp NAME = colexp(_R, "name");
    public static final ColumnExp MAINTAINER_ID = colexp(_R, "maintainerId");
    public static final ColumnExp STATUS = colexp(_R, "status");
    public static final ColumnExp HOST = colexp(_R, "host");
    public static final ColumnExp DEFINITION = colexp(_R, "definition");
    public static final ColumnExp DIGEST = colexp(_R, "digest");
    public static final ColumnExp DESCRIPTION = colexp(_R, "description");
    public static final ColumnExp INSTRUCTIONS = colexp(_R, "instructions");
    public static final ColumnExp CREDITS = colexp(_R, "credits");
    public static final ColumnExp CREATED = colexp(_R, "created");
    public static final ColumnExp LAST_UPDATED = colexp(_R, "lastUpdated");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 1;

    /** Defines the possible values for {@link #status}. */
    public enum Status { UNKNOWN, PENDING, READY, DISABLED }

    /** A unique integer identifier for this game. */
    @Id @Column(name="GAME_ID") @GeneratedValue(strategy=GenerationType.IDENTITY)
    public int gameId;

    /** A short string indicating the category of this game. */
    @Column(name="CATEGORY")
    public String category;

    /** The human readable name of this game. */
    @Column(name="NAME")
    public String name;

    /** The user id of the maintainer of this game. */
    @Column(name="MAINTAINER_ID") @Index
    public int maintainerId;

    /** The status of the game. We can't use the enumeration directly here as this class is
     * persisted and JORA doesn't (and can't be made to) automagically handle enums. */
    @Column(name="STATUS")
    public String status;

    /** The server on which this game is hosted. */
    @Column(name="HOST")
    public String host;

    /** The XML game definition associated with this version. */
    @Column(name="DEFINITION")
    public String definition;

    /** The MD5 digest of the game jar file. */
    @Column(name="DIGEST")
    public String digest;

    /** A brief description of the game. */
    @Column(name="DESCRIPTION")
    public String description;

    /** Brief instructions on how to play the game. */
    @Column(name="INSTRUCTIONS")
    public String instructions;

    /** Credits and license information for the game. */
    @Column(name="CREDITS")
    public String credits;

    /** The date on which the game was created. */
    @Column(name="CREATED")
    public Date created;

    /** The date on which the jar file was last updated. */
    @Column(name="LAST_UPDATED")
    public Date lastUpdated;

    /** Returns the status of this game. */
    public Status getStatus ()
    {
        return Status.valueOf(status);
    }

    /** Updates the status of this game. */
    public void setStatus (Status status)
    {
        this.status = status.toString();
    }

    /**
     * Parses this game's definition and returns
     */
    public GameDefinition parseGameDefinition ()
        throws InvocationException
    {
        if (_parser == null) {
            _parser = createParser();
        }

        try {
            GameDefinition gamedef;
            synchronized (_parser) {
                gamedef = _parser.parseGame(new StringReader(definition));
            }

            // fill in things that only we know
            gamedef.digest = digest;

            return gamedef;

        } catch (Exception e) {
//             log.warning("Failed to parse game definition [game=" + gameId + "]", e);
            throw (InvocationException)new InvocationException(
                ToyBoxCodes.ERR_MALFORMED_GAMEDEF).initCause(e);
        }
    }

    /**
     * Returns a brief description of this game.
     */
    public String which ()
    {
        return name + " (" + gameId + ")";
    }

    /**
     * Provides a string representation of this instance.
     */
    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /**
     * Creates the parser we'll use to turn our text configuration (probably XML) into a game
     * definition.
     */
    protected GameParser createParser ()
    {
        return new GameParser();
    }

    /** Used to parse our game definitions. */
    protected static GameParser _parser;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link GameRecord}
     * with the supplied key values.
     */
    public static Key<GameRecord> getKey (int gameId)
    {
        return newKey(_R, gameId);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(GAME_ID); }
    // AUTO-GENERATED: METHODS END
}
