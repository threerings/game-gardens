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

package com.threerings.toybox.lobby.client;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.micasa.client.OccupantList;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.TableMatchConfig;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.lobby.data.LobbyObject;
import com.threerings.toybox.lobby.table.TableListView;

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
    public LobbyPanel (ToyBoxContext ctx)
    {
        _ctx = ctx;

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

        // create a chat box and stick that in
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

    /**
     * Instructs the panel to create and display the match making view.
     */
    public void showMatchMakingView (LobbyConfig config)
    {
        // create our match-making view
        JComponent matchView = createMatchMakingView(_ctx, config);
        if (matchView != null) {
            _main.add(matchView, 0);
            if (matchView instanceof PlaceView) {
                // because we're adding our match making view after we've
                // already entered our place, we need to fake an entry
                ((PlaceView)matchView).willEnterPlace(_lobj);
            }
            SwingUtil.refresh(_main);
        }
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        _lobj = (LobbyObject)plobj;
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /**
     * Creates a custom view for the game we're match-making in this
     * lobby.
     */
    protected JComponent createMatchMakingView (
        ToyBoxContext ctx, LobbyConfig config)
    {
        GameDefinition gamedef = config.getGameDefinition();
        ToyBoxGameConfig gconfig = new ToyBoxGameConfig(gamedef);

        // we avoid putting this code into MatchConfig itself as that
        // introduces dependencies to all sorts of client side
        // user-interface code for any code that parses game definitions
        // or otherwise manipulates MatchConfig instances
        if (gamedef.match instanceof TableMatchConfig) {
            return new TableListView(ctx, gconfig);
        } else {
            return null;
        }
    }

    /** Giver of life and services. */
    protected ToyBoxContext _ctx;

    /** Contains the match-making view and the chatbox. */
    protected JPanel _main;

    /** Our lobby distributed object. */
    protected LobbyObject _lobj;

    /** Our occupant list display. */
    protected OccupantList _occupants;
}
