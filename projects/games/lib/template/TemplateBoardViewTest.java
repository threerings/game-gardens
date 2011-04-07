//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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
