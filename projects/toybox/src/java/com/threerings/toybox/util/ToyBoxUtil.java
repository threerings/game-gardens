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

package com.threerings.toybox.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import java.net.URL;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.Library;

import static com.threerings.toybox.Log.log;

/**
 * Various ToyBox utility methods.
 */
public class ToyBoxUtil
{
    /**
     * Creates a class loader with restricted permissions that loads
     * classes from the game jar and libraries specified by the supplied
     * game definition. Those jar files will be assumed to live relative
     * to the specified root directory.
     */
    public static ClassLoader createClassLoader (
        File root, GameDefinition gamedef)
    {
        ClassLoader loader = _cache.get(gamedef.ident);
        if (loader != null) {
            return loader;
        }

        ArrayList<URL> ulist = new ArrayList<URL>();
        String path = "";
        try {
            // add the game jar file
            path = "file://" + root + "/" + gamedef.getJarName();
            ulist.add(new URL(path));

            // enumerate the paths to the game's jar files
            for (int ii = 0; ii < gamedef.libs.length; ii++) {
                Library lib = gamedef.libs[ii];
                path = "file://" + root + "/" + lib.getURLPath();
                ulist.add(new URL(path));
            }

        } catch (Exception e) {
            log.warning("Failed to create URL for class loader " +
                        "[root=" + root + ", path=" + path +
                        ", error=" + e + "].");
        }

        // create and cache our new class loader
        _cache.put(gamedef.ident, loader = new ToyBoxClassLoader(
                       ulist.toArray(new URL[ulist.size()])));
        return loader;
    }

    /** We have to cache our classloaders on the client as we must
     * preserve the same classloader for the lifetime of the session so
     * that the class cache held by the ObjectInputStream remains
     * valid. On the server it is also useful as a single instance will
     * run multiple copies of the same game which might as well all use
     * the same copies of the code. We may end up changing this to allow
     * newer versions of the game to simultaneously exist with older
     * versions (on the same server). */
    protected static HashMap<String,ClassLoader> _cache =
        new HashMap<String,ClassLoader>();
}
