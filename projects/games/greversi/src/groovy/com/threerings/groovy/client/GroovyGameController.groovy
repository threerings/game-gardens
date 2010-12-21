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

package com.threerings.groovy.client;

import java.awt.BorderLayout;
import javax.swing.JComponent;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.util.ParlorContext;

/**
 * Bridges between the standard {@link GameController} and Groovy.
 */
public abstract class GroovyGameController extends GameController
{
    protected abstract JComponent createGameView (ParlorContext ctx);

    // @Override // from PlaceController
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        PlacePanel panel = new PlacePanel();
        panel.setLayout(new BorderLayout());
        panel.add(createGameView((ParlorContext)ctx), BorderLayout.CENTER);
        return panel;
    }
}
