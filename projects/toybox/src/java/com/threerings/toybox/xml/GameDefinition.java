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

package com.threerings.toybox.xml;

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

    /** The class name of the <code>GameConfig</code> derivation that we
     * use to launch the game on the client and server. */
    public String config;

    /** The configuration of the match-making mechanism. */
    public MatchConfig match;

    /** Parameters used to configure the game itself. */
    public Parameter[] params;

    /** The libraries required by this game. */
    public Library[] libs;

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
