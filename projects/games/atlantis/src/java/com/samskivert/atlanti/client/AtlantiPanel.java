//
// $Id

package com.samskivert.atlanti.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.SafeScrollPane;
import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.TileManager;
import com.threerings.util.MessageBundle;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.util.ToyBoxContext;

import com.samskivert.atlanti.Log;
import com.samskivert.atlanti.data.AtlantiCodes;
import com.samskivert.atlanti.data.AtlantiTile;
import com.samskivert.atlanti.util.PiecenUtil;

/**
 * The top-level user interface component for the game display.
 */
public class AtlantiPanel extends JPanel
    implements PlaceView, ControllerProvider, AtlantiCodes
{
    /** A reference to the board that is accessible to the controller. */
    public AtlantiBoardView board;

    /** A reference to our _noplace button. */
    public JButton noplace;

    /**
     * Constructs a new game display.
     */
    public AtlantiPanel (ToyBoxContext ctx, AtlantiController controller)
    {
	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	HGroupLayout gl = new HGroupLayout(
            HGroupLayout.STRETCH, 10, HGroupLayout.CENTER);
	gl.setOffAxisPolicy(HGroupLayout.STRETCH);
	setLayout(gl);

        // create the board
        board = new AtlantiBoardView();
        board.setOpaque(false);

        // create a scroll area to contain the board
        SafeScrollPane scrolly = new SafeScrollPane(board);
        scrolly.getViewport().setBackground(ToyBoxUI.LIGHT_BLUE);
        add(scrolly);

        // create our side panel
        VGroupLayout sgl = new VGroupLayout(VGroupLayout.STRETCH);
        sgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        sgl.setJustification(VGroupLayout.TOP);
        JPanel sidePanel = new JPanel(sgl);

        MessageBundle msgs =
            _ctx.getMessageManager().getBundle(ATLANTI_MESSAGE_BUNDLE);

        // add a big fat label because we love it!
        MultiLineLabel vlabel = new MultiLineLabel(msgs.get("m.title"));
        vlabel.setAntiAliased(true);
        vlabel.setFont(ToyBoxUI.fancyFont);
        sidePanel.add(vlabel, VGroupLayout.FIXED);

        // add a player info view to the side panel
        sidePanel.add(new JLabel("Scores:"), VGroupLayout.FIXED);
        sidePanel.add(new PlayerInfoView(), VGroupLayout.FIXED);

        // add a turn indicator to the side panel
        sidePanel.add(new JLabel("Current turn:"), VGroupLayout.FIXED);
        sidePanel.add(new TurnIndicatorView(), VGroupLayout.FIXED);

        // add a "place nothing" button
        noplace = new JButton("Place nothing");
        noplace.setEnabled(false);
        noplace.setActionCommand(PLACE_NOTHING);
        noplace.addActionListener(Controller.DISPATCHER);
        sidePanel.add(noplace, VGroupLayout.FIXED);

        // de-opaquify everything before we add the chat box
        SwingUtil.setOpaque(sidePanel, false);
        setOpaque(true);
        setBackground(new Color(0xDAEB9C));

        // add a chat box
        ChatPanel chat = new ChatPanel(ctx);
        chat.removeSendButton();
        sidePanel.add(chat);

        // add a "back" button
        JButton back = new JButton(
            ctx.xlate(ATLANTI_MESSAGE_BUNDLE, "m.back_to_lobby"));
        back.setActionCommand(BACK_TO_LOBBY);
        back.addActionListener(Controller.DISPATCHER);
        sidePanel.add(HGroupLayout.makeButtonBox(HGroupLayout.RIGHT, back),
                      VGroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);

        // we'll need these later
        _controller = controller;
        _ctx = ctx;
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        // we can't create our image manager until we have access to our
        // containing frame
        JRootPane rpane = getRootPane();
        ImageManager imgr = new ImageManager(
            _ctx.getToyBoxDirector().getResourceManager(), rpane);
        TileManager tmgr = new TileManager(imgr);
        AtlantiTile.setManagers(imgr, tmgr);
        PiecenUtil.init(tmgr);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        Log.info("Panel entered place.");
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        Log.info("Panel left place.");
    }

    // documentation inherited
    public Controller getController ()
    {
        return _controller;
    }

    /** Provides access to needed services. */
    protected ToyBoxContext _ctx;

    /** A reference to our controller that we need to implement the {@link
     * ControllerProvider} interface. */
    protected AtlantiController _controller;
}
