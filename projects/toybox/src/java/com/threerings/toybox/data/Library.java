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

import java.io.File;

import com.samskivert.util.StringUtil;
import com.threerings.io.Streamable;

/**
 * Defines a library dependency for a game.
 */
public class Library implements Streamable
{
    /** The name of the library (minus the version and .jar extension). */
    public String name;

    /** The version of the library on which this game depends. */
    public String version;

    /** An MD5 digest computed for this library. */
    public String digest;

    /**
     * Constructs the jar file name from the library name and version.
     */
    public String getJarName ()
    {
        return name + "-" + version + ".jar";
    }

    /**
     * Returns the file path relative to the resource root for this
     * library.
     */
    public String getFilePath ()
    {
        return ToyBoxCodes.LIBRARY_DIR + File.separator + getJarName();
    }

    /**
     * Returns the URL path relative to the resource root for this
     * library.
     */
    public String getURLPath ()
    {
        return ToyBoxCodes.LIBRARY_DIR + "/" + getJarName();
    }

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
