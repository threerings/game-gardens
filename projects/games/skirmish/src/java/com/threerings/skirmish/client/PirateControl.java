//
// $Id: PirateControl.java,v 1.5 2002/07/26 17:13:24 mdb Exp $

package com.threerings.skirmish.client;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.samskivert.swing.HGroupLayout;

import com.threerings.util.RandomUtil;

import com.threerings.skirmish.Log;

/**
 * Provides the interface for configuring a single pirate.
 */
public class PirateControl extends JPanel
    implements ActionListener, SkirmishController.Tickable
{
    public PirateControl (
        SkirmishController ctrl, TokenPanel tokpan, int puzType)
    {
        _tokpan = tokpan;
        _puzType = puzType;

        setLayout(new HGroupLayout());

        ButtonGroup group = new ButtonGroup();
        JRadioButton rbutton = new JRadioButton("Sailing");
        rbutton.setActionCommand("sailing");
        rbutton.addActionListener(this);
        if (puzType == TokenPanel.SAILING_TYPE) {
            rbutton.setSelected(true);
        }
        add(rbutton);
        group.add(rbutton);

        rbutton = new JRadioButton("Gunnery");
        rbutton.setActionCommand("gunnery");
        rbutton.addActionListener(this);
        if (puzType == TokenPanel.GUNNERY_TYPE) {
            rbutton.setSelected(true);
        }
        add(rbutton);
        group.add(rbutton);

        // register as a tickable
        ctrl.registerTickable(this);
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent event)
    {
        String cmd = event.getActionCommand();
        if (cmd.equals("sailing")) {
            _puzType = TokenPanel.SAILING_TYPE;
        } else if (cmd.equals("gunnery")) {
            _puzType = TokenPanel.GUNNERY_TYPE;
        } else {
            Log.warning("Received unknown action [cmd=" + cmd + "].");
        }
    }

    /**
     * Called ten times per second by the {@link SkirmishController} to
     * give us a chance to do our virtual piratey business.
     */
    public void tick ()
    {
        if (++_tickCount % 10 == 0) {
            // generate some amount of output for the puzzle type we
            // represent
            int output = RandomUtil.getInt(30);
            _tokpan.applyPuzzlingOutput(_puzType, output);
        }
    }

    protected TokenPanel _tokpan;
    protected int _puzType;
    protected int _tickCount;
}
