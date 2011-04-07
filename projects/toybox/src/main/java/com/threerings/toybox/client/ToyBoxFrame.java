//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.client;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ManagedJFrame;

/**
 * Contains the user interface for the ToyBox client application.
 */
public class ToyBoxFrame extends ManagedJFrame
    implements ToyBoxClient.Shell
{
    /**
     * Constructs the top-level ToyBox client frame with the specified window
     * title.
     */
    public ToyBoxFrame (String title, String gameId, String username)
    {
        super(title);

        // we use these to record our frame position and dimensions
        _gameId = gameId;
        _username = username;

        // we'll handle shutting things down ourselves
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // set up our initial frame size and position
        Rectangle bounds = ToyBoxPrefs.getClientBounds(gameId, username);
        if (bounds != null) {
            setBounds(bounds);
        } else {
            setSize(800, 600);
            SwingUtil.centerWindow(this);
        }
    }

    @Override // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        // listen for changes in size and position and record them
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized (ComponentEvent e) {
                ToyBoxPrefs.setClientBounds(_gameId, _username, getBounds());
            }
            @Override
            public void componentMoved (ComponentEvent e) {
                ToyBoxPrefs.setClientBounds(_gameId, _username, getBounds());
            }
        });
    }

    // from interface ToyBoxSession.Shell
    public void bindCloseAction (final ToyBoxClient client)
    {
        // log off when they close the window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (client.getContext().getClient().isLoggedOn()) {
                    client.getContext().getClient().logoff(true);
                }
                // and get the heck out
                System.exit(0);
            }
        });
    }

    protected String _gameId, _username;
}
