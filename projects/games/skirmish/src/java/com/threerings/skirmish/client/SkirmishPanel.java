//
// $Id: SkirmishPanel.java,v 1.11 2002/07/27 00:45:43 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.parlor.util.ParlorContext;
import com.threerings.toybox.data.ToyBoxGameConfig;

import com.threerings.micasa.client.ChatPanel;

/**
 * Contains the primary client interface for a Skirmish game.
 */
public class SkirmishPanel extends PlacePanel
{
    /** The token management interface. */
    public TokenPanel tpanel;

    /**
     * Creates a skirmish panel and its associated interface components.
     */
    public SkirmishPanel (ParlorContext ctx, ToyBoxGameConfig config,
                          SkirmishController ctrl)
    {
        super(ctrl);

        // keep this around for later
        _ctx = ctx;

        // create our various views and displays
        setLayout(new HGroupLayout(HGroupLayout.STRETCH, HGroupLayout.STRETCH,
                                   5, HGroupLayout.RIGHT));

        // create our token management interface (we'll add it later, but
        // we need it now to pass it to the pirate panel constructor)
        int tokenSpeed = (Integer)config.params.get("token_speed");
        tpanel = new TokenPanel(ctx, tokenSpeed);

        JPanel mpanel = new JPanel(
            new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH,
                             5, VGroupLayout.TOP) );
        mpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mpanel);

        // add a chat box
        mpanel.add(new ChatPanel(_ctx));

        // create our pirate control panel
        mpanel.add(new PiratePanel(ctrl, tpanel), VGroupLayout.FIXED);

        // the side bar panel will hold our main interface (in an attempt
        // to simulat the screen real estate we have when puzzling)
        JPanel spanel = new JPanel(
            new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH,
                             5, VGroupLayout.TOP));
        add(spanel, HGroupLayout.FIXED);

        // create our board view
        _bview = new SkirmishBoardView(ctx);
        spanel.add(_bview, VGroupLayout.FIXED);

        // add a timer view
        int turnInterval = (Integer)config.params.get("turn_interval");
        spanel.add(new TimerView(_ctx, ctrl, turnInterval), VGroupLayout.FIXED);

        // add some player views
        spanel.add(new PlayerView(0, _bview), VGroupLayout.FIXED);
        spanel.add(new PlayerView(1, _bview), VGroupLayout.FIXED);

        // add a status view
        int escapeDuration = (Integer)config.params.get("escape_duration");
        spanel.add(new StatusView(escapeDuration), VGroupLayout.FIXED);

        // add the token management interface
        spanel.add(tpanel, VGroupLayout.FIXED);

        // add a "back" button
        JButton back = new JButton("Back to lobby");
        back.setActionCommand(SkirmishController.BACK_TO_LOBBY);
        back.addActionListener(Controller.DISPATCHER);
        spanel.add(back, VGroupLayout.FIXED);

        setPreferredSize(new Dimension(800, 600));
    }

    /** Provides access to global client services. */
    protected ParlorContext _ctx;

    /** The board view. */
    protected SkirmishBoardView _bview;
}
