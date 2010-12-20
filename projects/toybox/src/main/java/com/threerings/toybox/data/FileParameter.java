//
// $Id$

package com.threerings.toybox.data;

import com.threerings.util.ActionScript;

import com.threerings.parlor.data.Parameter;

/**
 * Models a paramter that can be used to load the contents of a file into a byte array or string
 * and ship it to the server.
 */
@ActionScript(omit=true)
public class FileParameter extends Parameter
{
    /** Whether or not the contents of the file should be supplied as binary data. If false, the
     * file will be loaded as text in the platform default encoding . */
    public boolean binary = false;

    @Override // documentation inherited
    public String getLabel ()
    {
        return "m.file_" + ident;
    }

    @Override // documentation inherited
    public Object getDefaultValue ()
    {
        if (binary) {
            return new byte[0];
        } else {
            return "";
        }
    }
}
