//
// $Id$

package com.threerings.toybox.xml;

import com.samskivert.util.StringUtil;

/**
 * Defines a configuration parameter for a game. Various derived classes
 * exist that define particular types of configuration parameters
 * including choices, toggles, ranges, etc.
 */
public abstract class Parameter
{
    /** A string identifier that names this parameter. */
    public String ident;

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
