//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.groovy.client;

import java.awt.BorderLayout;
import javax.swing.JComponent;

import com.threerings.crowd.client.PlacePanel;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.util.ParlorContext;

/**
 * Bridges between the standard {@link GameController} and Groovy.
 */
public abstract class GroovyGameController extends GameController
{
    protected abstract JComponent createGameView (ParlorContext ctx);

    // @Override // from PlaceController
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        PlacePanel panel = new PlacePanel();
        panel.setLayout(new BorderLayout());
        panel.add(createGameView((ParlorContext)ctx), BorderLayout.CENTER);
        return panel;
    }
}
