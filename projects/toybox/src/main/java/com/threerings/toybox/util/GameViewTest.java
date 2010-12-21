//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JComponent;

import com.samskivert.util.Config;

import com.threerings.resource.ResourceManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJFrame;

import com.threerings.toybox.client.ToyBoxDirector;

/**
 * A test harness for one's game view which can be used to unit test a game
 * display outside the normal game interface (which is cumbersome to get into
 * and out of).
 */
public abstract class GameViewTest extends ManagedJFrame
{
    /**
     * Creates and displays the test interface.
     */
    public void display ()
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ToyBoxContext ctx = new ToyBoxContextImpl();
        _msgmgr = new MessageManager("rsrc.i18n");
        _framemgr = FrameManager.newInstance(this);
        _rsrcmgr = new ResourceManager("rsrc");
        _keydisp = new KeyDispatcher(this);

        getContentPane().add(createInterface(ctx), BorderLayout.CENTER);
        pack();
        setVisible(true);

        EventQueue.invokeLater(new Runnable() {
            public void run () {
                initInterface();
                _framemgr.start();
            }
        });
    }

    /**
     * Called by {@link #display} to create the game view component.
     */
    protected abstract JComponent createInterface (ToyBoxContext ctx);

    /**
     * Called after the interface is created on the AWT thread. This is where a
     * MediaPanel would be configured with sprites, etc.
     */
    protected void initInterface ()
    {
    }

    /**
     * The context implementation. This provides access to all of the
     * objects and services that are needed by the operating client.
     */
    protected class ToyBoxContextImpl extends ToyBoxContext
    {
        protected ToyBoxContextImpl () {
        }
        public Client getClient () {
            return null;
        }
        public DObjectManager getDObjectManager () {
            return null;
        }
        public Config getConfig () {
            return null;
        }
        public LocationDirector getLocationDirector () {
            return null;
        }
        public OccupantDirector getOccupantDirector () {
            return null;
        }
        public ChatDirector getChatDirector () {
            return null;
        }
        public ParlorDirector getParlorDirector () {
            return null;
        }
        public void setPlaceView (PlaceView view) {
            // not needed
        }
        public void clearPlaceView (PlaceView view) {
            // not needed
        }
        public ResourceManager getResourceManager () {
            return _rsrcmgr;
        }
        public MessageManager getMessageManager () {
            return _msgmgr;
        }
        public ToyBoxDirector getToyBoxDirector () {
            return null;
        }
        public FrameManager getFrameManager () {
            return _framemgr;
        }
        public KeyDispatcher getKeyDispatcher () {
            return _keydisp;
        }
    }

    protected MessageManager _msgmgr;
    protected FrameManager _framemgr;
    protected ResourceManager _rsrcmgr;
    protected KeyDispatcher _keydisp;
}
