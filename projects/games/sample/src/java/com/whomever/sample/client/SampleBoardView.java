//
// $Id: SampleBoardView.java,v 1.10 2002/07/27 00:45:43 mdb Exp $

package com.whomever.sample.client;

import java.awt.Graphics;
import javax.swing.JComponent;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.whomever.sample.data.SampleBoard;
import com.whomever.sample.data.SampleObject;

/**
 * Displays the Sample board and the vessels upon't.
 */
public class SampleBoardView extends JComponent
    implements PlaceView, AttributeChangeListener
{
    /**
     * Constructs a board view which will initialize itself and prepare to
     * display the Sample board.
     */
    public SampleBoardView (ToyBoxContext ctx)
    {
        _ctx = ctx;
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
        _sampobj = (SampleObject)plobj;
        _sampobj.addListener(this);
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        if (_sampobj != null) {
            _sampobj.removeListener(this);
            _sampobj = null;
        }
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (SampleObject.BOARD.equals(event.getName())) {
            // trigger a repaint the first time the board is set
            repaint();
        }
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // here we would render things, like our board and perhaps some
        // pieces or whatever is appropriate for this game
    }

    /** Provides access to client services. */
    protected ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected SampleObject _sampobj;
}
