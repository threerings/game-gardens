//
// $Id$

package com.threerings.toybox.xml;

import java.util.ArrayList;

import com.samskivert.util.StringUtil;

/**
 * Contains the information about a game as described by the game
 * definition XML file.
 */
public class GameDefinition
{
    /** A string identifier for the game. */
    public String ident;

    /** The class name of the <code>GameConfig</code> derivation that we
     * use to launch the game on the client and server. */
    public String config;

    /** The configuration of the match-making mechanism. */
    public MatchConfig match;

    /** Parameters used to configure the game itself. */
    public ArrayList<Parameter> params;

    /** The libraries required by this game. */
    public ArrayList<Library> libs;

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
