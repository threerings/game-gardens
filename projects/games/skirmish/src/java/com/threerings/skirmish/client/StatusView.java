//
// $Id: StatusView.java,v 1.5 2002/07/16 19:43:14 mdb Exp $

package com.threerings.skirmish.client;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.VGroupLayout;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.skirmish.Log;
import com.threerings.skirmish.data.SkirmishConfig;
import com.threerings.skirmish.data.SkirmishObject;

/**
 * Displays the damage level of the vessels in a skirmish.
 */
public class StatusView extends JPanel
    implements PlaceView, AttributeChangeListener, ElementUpdateListener
{
    public StatusView (SkirmishConfig skonfig)
    {
        _skonfig = skonfig;
        setLayout(new VGroupLayout());

        add(_elabel = new JLabel(""));
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _skobj = (SkirmishObject)plobj;
        _skobj.addListener(this);
        readStatus();
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_skobj != null) {
            _skobj.removeListener(this);
            _skobj = null;
        }
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(SkirmishObject.DAMAGE) ||
            event.getName().equals(SkirmishObject.ESCAPE_COUNTER) ||
            event.getName().equals(SkirmishObject.ATTACKER_INDEX)) {
            readStatus();
        }
    }

    // documentation inherited from interface
    public void elementUpdated (ElementUpdatedEvent event)
    {
        if (event.getName().equals(SkirmishObject.DAMAGE)) {
            readStatus();
        }
    }

    protected void readStatus ()
    {
        int eturns = (_skonfig.escapeDuration - _skobj.escapeCounter);
        if (eturns > 0) {
            _elabel.setText("Escape in " + eturns + " turns.");
        } else {
            _elabel.setText("Escaped!");
        }
    }

    protected SkirmishConfig _skonfig;
    protected SkirmishObject _skobj;
    protected JLabel _elabel;
}
