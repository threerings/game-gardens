//
// $Id$

package com.threerings.toybox.xml;

import com.samskivert.util.StringUtil;

/**
 * Used to configure the match-making interface for a game. Particular
 * match-making mechanisms extend this class and specify their own special
 * configuration parameters.
 */
public abstract class MatchConfig
{
    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
