//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.toybox.client;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.SimpleSlider;

import com.threerings.util.MessageBundle;

import com.threerings.parlor.client.GameConfigurator;

import com.threerings.toybox.data.ChoiceParameter;
import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.Parameter;
import com.threerings.toybox.data.RangeParameter;
import com.threerings.toybox.data.ToggleParameter;
import com.threerings.toybox.data.ToyBoxGameConfig;
import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.Log.log;

/**
 * Handles the configuration of a ToyBox game. This works in conjunction
 * with the {@link ToyBoxGameConfig} to provide a generic mechanism for
 * defining and obtaining game configuration settings.
 */
public class ToyBoxGameConfigurator extends GameConfigurator
{
    // documentation inherited
    protected void createConfigInterface ()
    {
        super.createConfigInterface();
    }

    // documentation inherited
    protected void gotGameConfig ()
    {
        super.gotGameConfig();

        ToyBoxContext ctx = (ToyBoxContext)_ctx;
        ToyBoxGameConfig config = (ToyBoxGameConfig)_config;
        GameDefinition gamedef = config.getGameDefinition();

        // create our parameter editors
        if (_editors == null) {
            // TODO: make sure this works when we're in the real
            // dynamically loaded toybox environment
            MessageBundle msgs = ctx.getMessageManager().getBundle(
                config.getBundleName());
            _editors = new ParamEditor[gamedef.params.length];
            for (int ii = 0; ii < _editors.length; ii++) {
                _editors[ii] = createEditor(msgs, gamedef.params[ii]);
                add((JPanel)_editors[ii]);
            }
        }

        // now read our parameters
        for (int ii = 0; ii < gamedef.params.length; ii++) {
            _editors[ii].readParameter(gamedef.params[ii], config);
        }
    }

    // documentation inherited
    protected void flushGameConfig ()
    {
        super.flushGameConfig();

        ToyBoxGameConfig config = (ToyBoxGameConfig)_config;
        GameDefinition gamedef = config.getGameDefinition();
        for (int ii = 0; ii < gamedef.params.length; ii++) {
            _editors[ii].writeParameter(gamedef.params[ii], config);
        }
    }

    protected ParamEditor createEditor (MessageBundle msgs, Parameter param)
    {
        if (param instanceof RangeParameter) {
            return new RangeEditor(msgs, (RangeParameter)param);
        } else if (param instanceof ToggleParameter) {
            return new ToggleEditor(msgs, (ToggleParameter)param);
        } else if (param instanceof ChoiceParameter) {
            return new ChoiceEditor(msgs, (ChoiceParameter)param);
        } else {
            log.warning("Unknown parameter type! " + param + ".");
            return null;
        }
    }

    /** Provides a uniform interface to our UI components. */
    protected static interface ParamEditor
    {
        public void readParameter (Parameter param, ToyBoxGameConfig config);
        public void writeParameter (Parameter param, ToyBoxGameConfig config);
    }

    protected class RangeEditor extends SimpleSlider implements ParamEditor
    {
        public RangeEditor (MessageBundle msgs, RangeParameter param)
        {
            super(msgs.get("m.range_" + param.ident),
                  param.minimum, param.maximum, param.start);

            getSlider().setPaintTicks(true);
            int range = param.maximum - param.minimum;
            getSlider().setMajorTickSpacing(Math.min(10, range));
        }

        public void readParameter (Parameter param, ToyBoxGameConfig config)
        {
            setValue((Integer)config.params.get(param.ident));
        }

        public void writeParameter (Parameter param, ToyBoxGameConfig config)
        {
            config.params.put(param.ident, getValue());
        }
    }

    protected class ToggleEditor extends JPanel implements ParamEditor
    {
        public ToggleEditor (MessageBundle msgs, ToggleParameter param)
        {
            setLayout(new HGroupLayout(HGroupLayout.NONE,
                                       HGroupLayout.LEFT));
            add(_box = new JCheckBox(
                    msgs.get("m.toggle_" + param.ident), param.start));
        }

        public void readParameter (Parameter param, ToyBoxGameConfig config)
        {
            _box.setSelected((Boolean)config.params.get(param.ident));
        }

        public void writeParameter (Parameter param, ToyBoxGameConfig config)
        {
            config.params.put(param.ident, _box.isSelected());
        }

        protected JCheckBox _box;
    }

    protected class ChoiceEditor extends JPanel implements ParamEditor
    {
        public ChoiceEditor (MessageBundle msgs, ChoiceParameter param)
        {
        }

        public void readParameter (Parameter param, ToyBoxGameConfig config)
        {
        }

        public void writeParameter (Parameter param, ToyBoxGameConfig config)
        {
        }
    }

    protected ParamEditor[] _editors;
}
