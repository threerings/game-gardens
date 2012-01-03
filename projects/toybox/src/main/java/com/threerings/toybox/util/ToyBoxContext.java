//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2012 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.threerings.media.FrameManager;
import com.threerings.media.image.ImageUtil;
import com.threerings.resource.ResourceManager;

import com.threerings.util.KeyDispatcher;
import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;
import com.threerings.util.Name;

import com.threerings.crowd.data.BodyObject;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.toybox.client.ToyBoxDirector;

import static com.threerings.toybox.Log.log;

/**
 * Aggregates the various bits that are needed on the ToyBox client.
 */
public abstract class ToyBoxContext implements ParlorContext
{
    /**
     * Returns a reference to the message manager used by the client to
     * generate localized messages.
     */
    public abstract MessageManager getMessageManager ();

    /**
     * Returns a reference to our ToyBox director.
     */
    public abstract ToyBoxDirector getToyBoxDirector ();

    /**
     * Returns a reference to our frame manager (used for media services).
     */
    public abstract FrameManager getFrameManager ();

    /**
     * Returns a reference to our key dispatcher.
     */
    public abstract KeyDispatcher getKeyDispatcher ();

    /**
     * Returns the resource manager which is used to load media resources.
     */
    public ResourceManager getResourceManager ()
    {
        return getToyBoxDirector().getResourceManager();
    }

    /**
     * Translates the specified message using the specified message bundle.
     */
    public String xlate (String bundle, String message)
    {
        MessageBundle mb = getMessageManager().getBundle(bundle);
        return (mb == null) ? message : mb.xlate(message);
    }

    /**
     * Convenience method to get the username of the currently logged on
     * user. Returns null when we're not logged on.
     */
    public Name getUsername ()
    {
        BodyObject bobj = (BodyObject)getClient().getClientObject();
        return (bobj == null) ? null : bobj.getVisibleName();
    }

    /**
     * Convenience method to load an image from our resource bundles.
     */
    public BufferedImage loadImage (String rsrcPath)
    {
        try {
            return getResourceManager().getImageResource(rsrcPath);
        } catch (IOException ioe) {
            log.warning("Unable to load image resource [path=" + rsrcPath + "].", ioe);
            // cope; return an error image of abitrary size
            return ImageUtil.createErrorImage(50, 50);
        }
    }
}
