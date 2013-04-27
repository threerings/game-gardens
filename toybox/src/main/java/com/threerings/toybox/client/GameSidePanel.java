//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.client;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.threerings.media.SafeScrollPane;
import com.threerings.util.MessageBundle;

import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.data.ToyBoxCodes.TOYBOX_MSGS;

/**
 * Contains standard widgets for a side panel in a game.
 */
public class GameSidePanel extends JPanel
{
    /**
     * Create a standard-issue GameSidePanel.
     */
    public GameSidePanel (ToyBoxContext ctx)
    {
        this(ctx, null);
    }

    /**
     * Create a standard-issue GameSidePanel with the specified pre-translated info text, which
     * may be HTML.
     */
    public GameSidePanel (final ToyBoxContext ctx, String info)
    {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        MessageBundle msgs = ctx.getMessageManager().getBundle(TOYBOX_MSGS);

        // for now, we use a tabbed pane, but maybe someday we'll have a niftier widget that acts
        // half like a pane with draggable resizing areas that can collapse, but also the collapsed
        // bits can be switched to like tabs
        JTabbedPane pane = new JTabbedPane();

        // we always add chat
        ChatPanel chat = new ChatPanel(ctx);
        chat.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        pane.addTab(msgs.get("m.chat_header"), chat);

        // add an occupant list
        // TODO: optionally filter game players?
        OccupantList occs = new OccupantList(ctx);
        occs.setOpaque(false);
        occs.setBorder(null);
        /*
        occs.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2),
            occs.getBorder()));
            */
        pane.addTab(msgs.get("m.occupant_header"), occs);

        // if there is a translation for info, we add that
        if (info != null) {
            JLabel label = new JLabel(info);
            label.setOpaque(false);
            label.setVerticalAlignment(JLabel.TOP);
            SafeScrollPane scroll = new SafeScrollPane(label, 50, 0);
            scroll.setBorder(null);
            pane.addTab(msgs.get("m.info_header"), scroll);
        }

        // create a back-to-lobby button
        JButton back = new JButton(msgs.get("m.back_to_lobby"));
        back.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                ctx.getLocationDirector().moveBack();
            }
        });

        // add the top-level elements to this panel
        add(pane, BorderLayout.CENTER);
        add(back, BorderLayout.SOUTH);
    }
}
