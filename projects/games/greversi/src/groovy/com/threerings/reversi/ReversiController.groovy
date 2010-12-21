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

package com.threerings.reversi;

import java.awt.BorderLayout;
import javax.swing.JComponent;

import groovy.swing.SwingBuilder;

import com.samskivert.swing.VGroupLayout;

import com.threerings.util.Name;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.groovy.client.GroovyGameController;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Manages the logic for a Reversi game.
 */
public class ReversiController extends GroovyGameController
{
    public void placePiece (Object source, ReversiPiece piece)
    {
        System.err.println("Place : " + piece);
    }

    // @Override // from GroovyGameController
    protected JComponent createGameView (ParlorContext ctx)
    {
        _view = new ReversiBoardView(ctx);
        return _swing.panel(layout:new BorderLayout()) {
            widget(widget:_view, constraints:BorderLayout.CENTER)
            panel(layout:new VGroupLayout(VGroupLayout.STRETCH),
                  constraints:BorderLayout.EAST) {
                widget(widget:new ChatPanel(ctx))
                button(text:"Back to Lobby", constraints:VGroupLayout.FIXED,
                       actionPerformed:{_ctx.getLocationDirector().moveBack()})
            }
        };
    }

    /** Displays the board interface. */
    protected ReversiBoardView _view;

    /** Used to build our interface. */
    protected SwingBuilder _swing = new SwingBuilder();
}
