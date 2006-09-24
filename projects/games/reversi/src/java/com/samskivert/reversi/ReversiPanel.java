//
// $Id$

package com.samskivert.reversi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.GroupLayout;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.util.MessageBundle;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Contains the primary client interface for the game.
 */
public class ReversiPanel extends PlacePanel
{
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

        // create and add our board view
        add(_bview = new ReversiBoardView(ctx), BorderLayout.CENTER);

        // create a side panel to hold our chat and other extra interfaces
        JPanel sidePanel = GroupLayout.makeVStretchBox(5);

        // add a big fat label
        JLabel vlabel = new JLabel(msgs.get("m.title"));
        vlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        vlabel.setForeground(Color.black);
        sidePanel.add(vlabel, GroupLayout.FIXED);

        // a score display or other useful status indicators can go here

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

    /** The board view. */
    protected ReversiBoardView _bview;
}
