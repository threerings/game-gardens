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

package com.threerings.toybox.data;

import java.util.ArrayList;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

/**
 * Contains the information about a game as described by the game
 * definition XML file.
 */
public class GameDefinition implements Streamable
{
    /** A string identifier for the game. */
    public String ident;

    /** The class name of the <code>GameController</code> derivation that
     * we use to bootstrap on the client. */
    public String controller;

    /** The class name of the <code>GameManager</code> derivation that we
     * use to manage the game on the server. */
    public String manager;

    /** The configuration of the match-making mechanism. */
    public MatchConfig match;

    /** Parameters used to configure the game itself. */
    public Parameter[] params;

    /** The libraries required by this game. */
    public Library[] libs;

    /** The MD5 digest of the game jar file. */
    public String digest;

    /**
     * Provides the name of the jar file associated with this game.
     *
     * @param gameId the unique id of the game provided when this game
     * definition was registered with the system, or -1 if we're running
     * in test mode.
     */
    public String getJarName (int gameId)
    {
        return (gameId == -1) ? ident + ".jar" : ident + "-" + gameId + ".jar";
    }

    /** Called when parsing a game definition from XML. */
    public void setParams (ArrayList<Parameter> list)
    {
        params = list.toArray(new Parameter[list.size()]);
    }

    /** Called when parsing a game definition from XML. */
    public void setLibs (ArrayList<Library> list)
    {
        libs = list.toArray(new Library[list.size()]);
    }

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
