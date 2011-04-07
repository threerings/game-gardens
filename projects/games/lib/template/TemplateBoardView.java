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

import java.awt.Graphics;
import javax.swing.JComponent;

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
