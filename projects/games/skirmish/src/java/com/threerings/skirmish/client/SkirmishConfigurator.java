//
// $Id: SkirmishConfigurator.java,v 1.4 2002/07/26 21:53:22 mdb Exp $

package com.threerings.skirmish.client;

import com.samskivert.swing.SimpleSlider;
import com.samskivert.swing.VGroupLayout;

import com.threerings.micasa.lobby.table.TableGameConfigurator;

import com.threerings.skirmish.data.SkirmishConfig;

/**
 * Provides a user interface for configuring a Skirmish game.
 */
public class SkirmishConfigurator extends TableGameConfigurator
{
    // documentation inherited
    protected void createConfigInterface ()
    {
        super.createConfigInterface();

        _boardSize = new SimpleSlider("Board size:", 16, 48, 32);
        add(_boardSize, VGroupLayout.FIXED);
        _handSize = new SimpleSlider("Hand size:", 2, 5, 5);
        add(_handSize, VGroupLayout.FIXED);
        _turnInterval = new SimpleSlider("Turn interval (s):", 10, 90, 20);
        add(_turnInterval, VGroupLayout.FIXED);
        _escapeDuration = new SimpleSlider("Escape turns:", 5, 25, 12);
        add(_escapeDuration, VGroupLayout.FIXED);
        _handicap = new SimpleSlider("Handicap (-att, +def):", -9, 9, 0);
        add(_handicap, VGroupLayout.FIXED);
        _featureDensity = new SimpleSlider("Feature density:", 0, 15, 7);
        add(_featureDensity, VGroupLayout.FIXED);
        _tokenSpeed = new SimpleSlider("Token speed:", 1, 10, 5);
        add(_tokenSpeed, VGroupLayout.FIXED);
    }

    // documentation inherited
    protected void gotGameConfig ()
    {
        super.gotGameConfig();
        _sconfig = (SkirmishConfig)_config;

        // configure our elements
        _boardSize.setValue(_sconfig.boardWidth);
        _handSize.setValue(_sconfig.handSize);
        _turnInterval.setValue(_sconfig.turnInterval);
        _escapeDuration.setValue(_sconfig.escapeDuration);
        _handicap.setValue(_sconfig.handicap);
        _featureDensity.setValue(_sconfig.featureDensity);
        _tokenSpeed.setValue(_sconfig.tokenSpeed);
    }

    // documentation inherited
    protected void flushGameConfig ()
    {
        super.flushGameConfig();

        // configure our elements
        _sconfig.boardWidth = _boardSize.getValue();
        _sconfig.boardHeight = _boardSize.getValue();
        _sconfig.handSize = _handSize.getValue();
        _sconfig.turnInterval = _turnInterval.getValue();
        _sconfig.escapeDuration = _escapeDuration.getValue();
        _sconfig.handicap = _handicap.getValue();
        _sconfig.featureDensity = _featureDensity.getValue();
        _sconfig.tokenSpeed = _tokenSpeed.getValue();
    }

    protected SkirmishConfig _sconfig;
    protected SimpleSlider _boardSize;
    protected SimpleSlider _handSize;
    protected SimpleSlider _turnInterval;
    protected SimpleSlider _escapeDuration;
    protected SimpleSlider _handicap;
    protected SimpleSlider _featureDensity;
    protected SimpleSlider _tokenSpeed;
}
