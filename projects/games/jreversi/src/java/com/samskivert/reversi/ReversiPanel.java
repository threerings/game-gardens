//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/jreversi/LICENSE

package com.samskivert.reversi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.ShapeIcon;
import com.samskivert.swing.MultiLineLabel;

import com.threerings.util.MessageBundle;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.parlor.turn.client.TurnDisplay;

import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Contains the primary client interface for the game.
 */
public class ReversiPanel extends PlacePanel
{
    /** The board view. */
    public ReversiBoardView bview;

    /**
     * Creates a Reversi panel and its associated interface components.
     */
    public ReversiPanel (ToyBoxContext ctx, ReversiController ctrl)
    {
        super(ctrl);
        _ctx = ctx;

        // this is used to look up localized strings
        MessageBundle msgs = _ctx.getMessageManager().getBundle("reversi");

        // give ourselves a wee bit of a border
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

        // give ourself a soothing blue background
        setBackground(new Color(0x6699CC));

        // create a container that will hold our board view in its center
        JPanel box = GroupLayout.makeHBox();
        // create and add our board view
        box.add(bview = new ReversiBoardView(ctx, ctrl));
        box.setOpaque(false);
        add(box, BorderLayout.CENTER);

        // create a side panel to hold our chat and other extra interfaces
        JPanel sidePanel = GroupLayout.makeVStretchBox(5);
        sidePanel.setOpaque(false);

        // add a big fat label
        MultiLineLabel vlabel = new MultiLineLabel(msgs.get("m.title"));
        vlabel.setFont(ToyBoxUI.fancyFont);
        sidePanel.add(vlabel, GroupLayout.FIXED);

        // add a standard turn display
        TurnDisplay turnDisplay = new TurnDisplay();
        turnDisplay.setOpaque(false);
        Polygon triangle = new Polygon(new int[] { 0, 12, 0 },
                                       new int[] { 0, 6, 12 }, 3);
        turnDisplay.setTurnIcon(new ShapeIcon(triangle, Color.yellow, null));
        turnDisplay.setWinnerText(ctx.xlate("reversi", "m.winner"));
        turnDisplay.setDrawText(ctx.xlate("reversi", "m.draw"));
        Ellipse2D lips = new Ellipse2D.Float(0, 0, 12, 12);
        turnDisplay.setPlayerIcons(new Icon[] {
            new ShapeIcon(lips, Color.black, null),
            new ShapeIcon(lips, Color.white, null) });
        sidePanel.add(turnDisplay, GroupLayout.FIXED);

        // add a chat box
        sidePanel.add(new ChatPanel(ctx));

        // add a "back to lobby" button
        JButton back = ReversiController.createActionButton(
            msgs.get("m.back_to_lobby"), "backToLobby");
        sidePanel.add(back, GroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, BorderLayout.EAST);
    }

    /** Provides access to various client services. */
    protected ToyBoxContext _ctx;
}
