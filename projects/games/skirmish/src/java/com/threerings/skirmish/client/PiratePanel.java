//
// $Id: PiratePanel.java,v 1.3 2002/07/26 17:14:47 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

/**
 * Displays and simulates a horde of pirates puzzling away and generating
 * skirmish tokens.
 */
public class PiratePanel extends JPanel
    implements ActionListener
{
    public PiratePanel (SkirmishController ctrl, TokenPanel tokpan)
    {
        _tokpan = tokpan;

        setLayout(new VGroupLayout());

//         // create our left/forward/right radio buttons
//         add(new JLabel("Wheel control"));
//         JPanel hgroup = GroupLayout.makeHBox();
//         add(hgroup);

//         ButtonGroup group = new ButtonGroup();
//         JRadioButton rbutton = new JRadioButton("Left");
//         rbutton.setActionCommand("left");
//         rbutton.addActionListener(this);
//         hgroup.add(rbutton);
//         group.add(rbutton);

//         rbutton = new JRadioButton("Forward");
//         rbutton.setActionCommand("forward");
//         rbutton.addActionListener(this);
//         rbutton.setSelected(true);
//         hgroup.add(rbutton);
//         group.add(rbutton);

//         rbutton = new JRadioButton("Right");
//         rbutton.setActionCommand("right");
//         rbutton.addActionListener(this);
//         hgroup.add(rbutton);
//         group.add(rbutton);

        // now create some virtual pirates
        add(new JLabel("Virtual pirates"));
        _pcontrols = new PirateControl[VIRTUAL_PIRATE_TYPE.length];
        for (int ii = 0; ii < VIRTUAL_PIRATE_TYPE.length; ii++) {
            _pcontrols[ii] = new PirateControl(ctrl, tokpan,
                                               VIRTUAL_PIRATE_TYPE[ii]);
            SwingUtil.setEnabled(_pcontrols[ii], VIRTUAL_PIRATE_ACTIVE[ii]);
            add(_pcontrols[ii]);
        }
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent event)
    {
        String cmd = event.getActionCommand();
        if (cmd.equals("left")) {
            _tokpan.setWheelState(TokenPanel.WHEEL_LEFT);
        } else if (cmd.equals("forward")) {
            _tokpan.setWheelState(TokenPanel.WHEEL_CENTER);
        } else if (cmd.equals("right")) {
            _tokpan.setWheelState(TokenPanel.WHEEL_RIGHT);
        }
    }

    /** The token panel to which we'll be delivering tokens. */
    protected TokenPanel _tokpan;

    /** Our virtual pirate controls. */
    protected PirateControl[] _pcontrols;

    /** Used when creating virtual pirates. */
    protected static final int[] VIRTUAL_PIRATE_TYPE = {
        TokenPanel.SAILING_TYPE, TokenPanel.SAILING_TYPE,
        TokenPanel.GUNNERY_TYPE };

    /** Used when creating virtual pirates. */
    protected static final boolean[] VIRTUAL_PIRATE_ACTIVE = {
        false, true, false };
}
