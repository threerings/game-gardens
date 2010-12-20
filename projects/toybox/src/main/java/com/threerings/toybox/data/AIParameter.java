//
// $Id$

package com.threerings.toybox.data;

import com.threerings.util.ActionScript;

import com.threerings.parlor.data.Parameter;

/**
 * Models a parameter that is used to configure AIs.
 */
@ActionScript(omit=true)
public class AIParameter extends Parameter
{
    /** Indicates the maximum number of AIs in the game. */
    public int maximum;

    // TODO: allow specification of difficulty range
    // TODO: allow specification of personality types

    @Override // documentation inherited
    public String getLabel ()
    {
        return "m.ai_" + ident;
    }

    @Override // documentation inherited
    public Object getDefaultValue ()
    {
        return 0;
    }
}
