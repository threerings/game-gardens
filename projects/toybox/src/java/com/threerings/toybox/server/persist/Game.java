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

import java.io.StringReader;
import java.sql.Timestamp;
import java.util.logging.Level;

import com.samskivert.util.StringUtil;

import com.threerings.presents.server.InvocationException;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.xml.GameParser;

import static com.threerings.toybox.Log.log;

/**
 * Contains information about a game registration.
 */
public class Game
{
    /** Defines the possible values for {@link #status}. */
    public enum Status { UNKNOWN, PENDING, READY, DISABLED };

    /** A unique integer identifier for this game. */
    public int gameId;

    /** A short string indicating the category of this game. */
    public String category;

    /** The human readable name of this game. */
    public String name;

    /** The user id of the maintainer of this game. */
    public int maintainerId;

    /** The status of the game. We can't use the enumeration directly here
     * as this class is persisted and JORA doesn't (and can't be made to)
     * automagically handle enums. */
    public String status;

    /** The server on which this game is hosted. */
    public String host;

    /** The XML game definition associated with this version. */
    public String definition;

    /** The MD5 digest of the game jar file. */
    public String digest;

    /** A brief description of the game. */
    public String description;

    /** Brief instructions on how to play the game. */
    public String instructions;

    /** The time at which the jar file was last updated. */
    public Timestamp lastUpdated;

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
            _parser = new GameParser();
        }

        try {
            GameDefinition gamedef = _parser.parseGame(
                new StringReader(definition));

            // fill in things that only we know
            gamedef.digest = digest;

            return gamedef;

        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to parse game definition " +
                    "[game=" + gameId + "]", e);
            throw new InvocationException(ToyBoxCodes.ERR_MALFORMED_GAMEDEF);
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
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /** Used to parse our game definitions. */
    protected static GameParser _parser;
}
