//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/greversi/LICENSE

package com.threerings.reversi;

import java.awt.Graphics2D;

import com.threerings.media.sprite.action.CommandSprite;

/**
 * Used to display a potential move to the player.
 */
public class PlacingSprite extends ReversiPieceSprite
    implements CommandSprite
{
    public PlacingSprite (ReversiPiece piece)
    {
        super(piece);
    }

    // @Override // from Sprite
    public void paint (Graphics2D gfx)
    {
        // TODO: set alpha
        super.paint(gfx);
        // TODO: clear alpha
    }

    // from CommandSprite
    public String getActionCommand ()
    {
        return "placePiece";
    }

    // from CommandSprite
    public Object getCommandArgument()
    {
        return _piece;
    }
}
