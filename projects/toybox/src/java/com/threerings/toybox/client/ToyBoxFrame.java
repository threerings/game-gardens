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

package com.threerings.toybox.client;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ManagedJFrame;

/**
 * Contains the user interface for the ToyBox client application.
 */
public class ToyBoxFrame extends ManagedJFrame
    implements ControllerProvider
{
    /**
     * Constructs the top-level ToyBox client frame with the specified
     * window title.
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

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        // listen for changes in size and position and record them
        addComponentListener(new ComponentAdapter() {
            public void componentResized (ComponentEvent e) {
                ToyBoxPrefs.setClientBounds(_gameId, _username, getBounds());
            }
            public void componentMoved (ComponentEvent e) {
                ToyBoxPrefs.setClientBounds(_gameId, _username, getBounds());
            }
        });
    }

    /**
     * Sets the panel that makes up the entire client display.
     */
    public void setPanel (JPanel panel)
    {
        // remove the old panel
        getContentPane().removeAll();
	// add the new one
	getContentPane().add(panel, BorderLayout.CENTER);
        // swing doesn't properly repaint after adding/removing children
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Sets the controller for the outermost scope. This controller will
     * handle all actions that aren't handled by controllers of tigher
     * scope.
     */
    public void setController (Controller controller)
    {
        _controller = controller;
    }

    // documentation inherited
    public Controller getController ()
    {
        return _controller;
    }

    protected Controller _controller;
    protected String _gameId, _username;
}
