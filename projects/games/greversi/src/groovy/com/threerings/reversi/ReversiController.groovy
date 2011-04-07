//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

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
