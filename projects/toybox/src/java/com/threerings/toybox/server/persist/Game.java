//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import com.samskivert.util.StringUtil;

import com.threerings.presents.server.InvocationException;

import com.threerings.toybox.Log;
import com.threerings.toybox.data.ToyBoxCodes;
import com.threerings.toybox.xml.GameDefinition;
import com.threerings.toybox.xml.GameParser;

/**
 * Contains information about a game registration.
 */
public class Game
{
    /** Defines the possible values for {@link #status}. */
    public enum Status { UNKNOWN, PENDING, PUBLISHED, DISABLED };

    /** A unique integer identifier for this game. */
    public int gameId;

    /** A string used to identify this game. */
    public String ident;

    /** The user id of the maintainer of this game. */
    public int maintainerId;

    /** The status of the game. */
    public Status status;

    /** The server on which this game is hosted. */
    public String host;

    /** The XML game definition associated with this version. */
    public String definition;

    /** The XML game definition associated with the test version. */
    public String testDefinition;

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
            return _parser.parseGame(new StringReader(definition));
        } catch (Exception e) {
            Log.warning("Failed to parse game definition " +
                        "[ident=" + ident + "]", e);
            throw new InvocationException(ToyBoxCodes.ERR_MALFORMED_GAMEDEF);
        }
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
