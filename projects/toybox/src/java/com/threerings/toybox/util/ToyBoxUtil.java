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

import java.net.URL;
import java.net.URLClassLoader;

import com.threerings.resource.ResourceManager;

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

        ClassLoader loader = new URLClassLoader(
            ulist.toArray(new URL[ulist.size()]),
            ToyBoxUtil.class.getClassLoader());
        log.info("Created " + loader + " with parent " +
                 ToyBoxUtil.class.getClassLoader() + ".");

        try {
            ResourceManager rmgr = new ResourceManager("rsrc", loader);
            log.info("IR: " + rmgr.getImageResource("media/piecens.png"));
        } catch (Exception e) {
            log.warning("Gurk: " + e);
        }

        return loader;
    }
}
