//
// $Id$

package com.threerings.toybox.xml;

import com.samskivert.util.StringUtil;

/**
 * Defines a library dependency for a game.
 */
public class Library
{
    /** The name of the library (minus the version and .jar extension). */
    public String name;

    /** The version of the library on which this game depends. */
    public String version;

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
