//
// $Id: SamplePanel.java,v 1.11 2002/07/27 00:45:43 mdb Exp $

package com.whomever.sample.client;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.media.SafeScrollPane;
import com.threerings.util.MessageBundle;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.whomever.sample.data.SampleCodes;

/**
 * Contains the primary client interface for a Sample game.
 */
public class SamplePanel extends PlacePanel
{
    /**
     * Creates a Sample panel and its associated interface components.
     */
    public SamplePanel (ToyBoxContext ctx, ToyBoxGameConfig config,
                        SampleController ctrl)
    {
        super(ctrl);
        _ctx = ctx;

        MessageBundle msgs = _ctx.getMessageManager().getBundle(
            SampleCodes.SAMPLE_MSG_BUNDLE);

        // give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
	gl.setOffAxisPolicy(HGroupLayout.STRETCH);
	setLayout(gl);

        // create the board and a scroll area to contain the board
        _bview = new SampleBoardView(ctx);
        SafeScrollPane scrolly = new SafeScrollPane(_bview);
        add(scrolly);

        // create our side panel
        VGroupLayout sgl = new VGroupLayout(VGroupLayout.STRETCH);
        sgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        sgl.setJustification(VGroupLayout.TOP);
        JPanel sidePanel = new JPanel(sgl);

        // add a big fat label
        JLabel vlabel = new JLabel("Sample Game!");
        vlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        vlabel.setForeground(Color.black);
        sidePanel.add(vlabel, VGroupLayout.FIXED);

        // things like a score indicator and other useful status
        // indicators might go here

        // add a chat box
        ChatPanel chat = new ChatPanel(ctx);
        chat.removeSendButton();
        sidePanel.add(chat);

        // add a "back" button
        JButton back = new JButton("Back to lobby");
        back.setActionCommand(SampleController.BACK_TO_LOBBY);
        back.addActionListener(SampleController.DISPATCHER);
        sidePanel.add(back, VGroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);
    }

    /** Provides access to various client services. */
    protected ToyBoxContext _ctx;

    /** The board view. */
    protected SampleBoardView _bview;
}
