//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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

package @package@;

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
public class @classpre@Panel extends PlacePanel
{
    /**
     * Creates a @classpre@ panel and its associated interface components.
     */
    public @classpre@Panel (ToyBoxContext ctx, @classpre@Controller ctrl)
    {
        super(ctrl);
        _ctx = ctx;

        // this is used to look up localized strings
        MessageBundle msgs = _ctx.getMessageManager().getBundle("@project@");

        // give ourselves a wee bit of a border
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

        // create and add our board view
        add(_bview = new @classpre@BoardView(ctx), BorderLayout.CENTER);

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
        JButton back = @classpre@Controller.createActionButton(
            msgs.get("m.back_to_lobby"), "backToLobby");
        sidePanel.add(back, GroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, BorderLayout.EAST);
    }

    /** Provides access to various client services. */
    protected ToyBoxContext _ctx;

    /** The board view. */
    protected @classpre@BoardView _bview;
}
