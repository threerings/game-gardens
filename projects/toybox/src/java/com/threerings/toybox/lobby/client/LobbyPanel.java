//
// $Id: LobbyPanel.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.lobby.client;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.micasa.client.ChatPanel;
import com.threerings.micasa.client.OccupantList;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Displays the main ToyBox match-making lobby interface.
 */
public class LobbyPanel extends JPanel
    implements PlaceView
{
    /**
     * Constructs a new lobby panel and the associated user interface
     * elements.
     */
    public LobbyPanel (ToyBoxContext ctx, LobbyConfig config)
    {
        // we want a five pixel border around everything
    	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create our primary layout which divides the display in two
        // horizontally
        GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        gl.setJustification(GroupLayout.RIGHT);
        setLayout(gl);

        // create our main panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        _main = new JPanel(gl);

        // create our match-making view
        _main.add(config.createMatchMakingView(ctx));

        // create a chat box and stick that in as well
        _main.add(new ChatPanel(ctx));

        // now add the main panel into the mix
        add(_main);

        // create our sidebar panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        JPanel sidePanel = new JPanel(gl);

        // the sidebar contains an occupants list
        JLabel label = new JLabel("People in lobby");
        sidePanel.add(label, GroupLayout.FIXED);
        _occupants = new OccupantList(ctx);
        sidePanel.add(_occupants);

        JButton logoff = new JButton("Logoff");
        logoff.addActionListener(LobbyController.DISPATCHER);
        logoff.setActionCommand("logoff");
        sidePanel.add(logoff, GroupLayout.FIXED);

        // add our sidebar panel into the mix
        add(sidePanel, GroupLayout.FIXED);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /** Contains the match-making view and the chatbox. */
    protected JPanel _main;

    /** Our occupant list display. */
    protected OccupantList _occupants;
}
