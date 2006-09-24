//
// $Id$

package @package@;

import javax.swing.JComponent;

import com.threerings.toybox.util.GameViewTest;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * A test harness for our board view.
 */
public class @classpre@BoardViewTest extends GameViewTest
{
    public static void main (String[] args)
    {
        @classpre@BoardViewTest test = new @classpre@BoardViewTest();
        test.display();
    }

    protected JComponent createInterface (ToyBoxContext ctx)
    {
        return _view = new @classpre@BoardView(ctx);
    }

    protected void initInterface ()
    {
        // add sprites and other media to the board view here
    }

    protected @classpre@BoardView _view;
}
