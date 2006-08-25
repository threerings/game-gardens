//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005 Three Rings Design, Inc., All Rights Reserved
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
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

/**
 * The top-level user interface component of the ToyBox client. This is either
 * stuffed into a frame when running in application mode or stuck into an
 * applet when running in applet mode.
 */
public class ToyBoxShell extends JPanel
    implements ControllerProvider
{
    public ToyBoxShell ()
    {
        super(new BorderLayout());
    }

    /**
     * Sets the panel that makes up the entire client display.
     */
    public void setPanel (JPanel panel)
    {
        // remove the old panel
        removeAll();
	// add the new one
	add(panel, BorderLayout.CENTER);
        // swing doesn't properly repaint after adding/removing children
        revalidate();
        repaint();
    }

    /**
     * Sets the controller for the outermost scope. This controller will handle
     * all actions that aren't handled by controllers of tigher scope.
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
}
