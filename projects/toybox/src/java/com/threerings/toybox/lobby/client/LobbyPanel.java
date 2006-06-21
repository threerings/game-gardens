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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import java.util.logging.Level;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.Spacer;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.image.BufferedMirage;
import com.threerings.media.image.ImageUtil;
import com.threerings.media.image.Mirage;
import com.threerings.util.MessageBundle;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.client.OccupantList;
import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.TableMatchConfig;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.toybox.lobby.data.LobbyCodes;
import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.lobby.data.LobbyObject;
import com.threerings.toybox.lobby.table.TableListView;

import static com.threerings.toybox.lobby.Log.log;

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
        _msgs = _ctx.getMessageManager().getBundle(LobbyCodes.LOBBY_MSGS);

        // we want a five pixel border around everything
    	setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        setLayout(new BorderLayout(10, 10));

        // create our main panel
        GroupLayout gl = new VGroupLayout(
            GroupLayout.STRETCH, 10, GroupLayout.TOP);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        _main = new JPanel(gl);

        // create a chat box and stick that in
        _main.add(new ChatPanel(ctx, true));

        // set up the title and main panel
        _title = new MultiLineLabel("", MultiLineLabel.CENTER);
        _title.setFont(ToyBoxUI.fancyFont);
        _title.setAntiAliased(true);
        add(_title, BorderLayout.NORTH);
        add(_main, BorderLayout.CENTER);

        // create our sidebar panel
        gl = new VGroupLayout(GroupLayout.STRETCH);
        JPanel sidePanel = new JPanel(gl);

        JLabel label = new JLabel(_msgs.get("m.occupants"));
        sidePanel.add(label, GroupLayout.FIXED);
        sidePanel.add(_occupants = new OccupantList(ctx));

        JButton logoff = new JButton(_msgs.get("m.logoff"));
        logoff.addActionListener(LobbyController.DISPATCHER);
        logoff.setActionCommand("logoff");
        sidePanel.add(logoff, GroupLayout.FIXED);

        // add our sidebar panel into the mix
        add(sidePanel, BorderLayout.EAST);

        // load up our background image
        try {
            InputStream bgin = getClass().getClassLoader().getResourceAsStream(
                "rsrc/media/lobby_background.png");
            if (bgin != null) {
                _bgimg = new BufferedMirage(ImageIO.read(bgin));
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load background image.", e);
        }

        // properly configure all of our components
        SwingUtil.applyToHierarchy(this, _colorizer);
    }

    /**
     * Instructs the panel to create and display the match making view.
     */
    public void showMatchMakingView (LobbyConfig config)
    {
        // create our match-making view
        JComponent matchView = createMatchMakingView(_ctx, config);
        if (matchView != null) {
            _main.add(matchView, GroupLayout.FIXED, 0);
            if (matchView instanceof PlaceView) {
                // because we're adding our match making view after we've
                // already entered our place, we need to fake an entry
                ((PlaceView)matchView).willEnterPlace(_lobj);
            }
            // properly configure all of our components (limiting to a
            // depth of six is a giant hack but I'm too lazy to do the
            // serious dicking around that is needed to do the "right"
            // thing; fucking user interfaces)
            SwingUtil.applyToHierarchy(this, 6, _colorizer);
            SwingUtil.refresh(_main);
        }
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        _lobj = (LobbyObject)plobj;
        _title.setText(_lobj.name);
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    // documentation inherited
    protected void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // tile our background image
        if (_bgimg != null) {
            Graphics2D gfx = (Graphics2D)g;
            ImageUtil.tileImage(gfx, _bgimg, 0, 0, getWidth(), getHeight());
        }
    }

    /**
     * Creates a custom view for the game we're match-making in this
     * lobby.
     */
    protected JComponent createMatchMakingView (
        ToyBoxContext ctx, LobbyConfig config)
    {
        GameDefinition gamedef = config.getGameDefinition();
        ToyBoxGameConfig gconfig = new ToyBoxGameConfig(
            config.getGameId(), gamedef);

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

    /** Used to de-opaquify and set the right backgrounds in the right
     * places. */
    protected SwingUtil.ComponentOp _colorizer = new SwingUtil.ComponentOp() {
        public void apply (Component comp) {
            if (comp instanceof JPanel) {
                ((JPanel)comp).setOpaque(false);
            } else if (comp instanceof JCheckBox) {
                ((JCheckBox)comp).setOpaque(false);
                comp.setForeground(Color.white);
            } else if (comp instanceof JSlider) {
                ((JSlider)comp).setOpaque(false);
                comp.setForeground(Color.white);
            } else if (comp instanceof JScrollPane) {
                ((JScrollPane)comp).getViewport().setBackground(
                    ToyBoxUI.LIGHT_BLUE);
            } else if (comp instanceof JLabel) {
                comp.setForeground(Color.white);
            } else if (comp instanceof JList ||
                       comp instanceof JComboBox) {
                comp.setBackground(ToyBoxUI.LIGHT_BLUE);
            }
        }
    };

    /** Giver of life and services. */
    protected ToyBoxContext _ctx;

    /** Our translation messages. */
    protected MessageBundle _msgs;

    /** Contains the match-making view and the chatbox. */
    protected JPanel _main;

    /** Our lobby distributed object. */
    protected LobbyObject _lobj;

    /** Displays the game title. */
    protected MultiLineLabel _title;

    /** Our occupant list display. */
    protected OccupantList _occupants;

    /** Our background image. */
    protected Mirage _bgimg;
}
