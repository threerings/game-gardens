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

import com.threerings.media.FrameManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.MessageManager;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.toybox.client.ToyBoxDirector;

/**
 * Aggregates the various bits that are needed on the ToyBox client.
 */
public interface ToyBoxContext extends ParlorContext
{
    /**
     * Returns a reference to the message manager used by the client to
     * generate localized messages.
     */
    public MessageManager getMessageManager ();

    /**
     * Returns a reference to our ToyBox director.
     */
    public ToyBoxDirector getToyBoxDirector ();

    /**
     * Returns a reference to our frame manager (used for media services).
     */
    public FrameManager getFrameManager ();

    /**
     * Returns a reference to our key dispatcher.
     */
    public KeyDispatcher getKeyDispatcher ();
}
