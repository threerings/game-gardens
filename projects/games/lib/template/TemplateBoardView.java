//
// $Id$

package @package@;

import java.awt.Graphics;
import javax.swing.JComponent;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.toybox.util.ToyBoxContext;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

/**
 * Displays the main game interface (the board).
 */
public class @classpre@BoardView extends JComponent
    implements PlaceView
{
    /**
     * Constructs a view which will initialize itself and prepare to display
     * the game board.
     */
    public @classpre@BoardView (ToyBoxContext ctx)
    {
        _ctx = ctx;
    }

    // from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
        _gameobj = (@classpre@Object)plobj;
    }

    // from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    @Override // from JComponent
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // here we would render things, like our board and perhaps some
        // pieces or whatever is appropriate for this game
    }

    /** Provides access to client services. */
    protected ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected @classpre@Object _gameobj;
}
