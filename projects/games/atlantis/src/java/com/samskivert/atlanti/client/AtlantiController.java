//
// $Id

package com.samskivert.atlanti.client;

import java.awt.event.ActionEvent;
import com.samskivert.util.ListUtil;

import com.threerings.util.Name;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DynamicListener;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;

import com.threerings.toybox.util.ToyBoxContext;

import com.samskivert.atlanti.Log;
import com.samskivert.atlanti.data.AtlantiCodes;
import com.samskivert.atlanti.data.AtlantiObject;
import com.samskivert.atlanti.data.AtlantiTile;
import com.samskivert.atlanti.data.Piecen;
import com.samskivert.atlanti.util.TileUtil;

/**
 * The main coordinator of user interface activities on the client-side of
 * the game.
 */
public class AtlantiController extends GameController
    implements TurnGameController, AtlantiCodes
{
    /**
     * Creates our controller and prepares it for operation.
     */
    public AtlantiController ()
    {
        addDelegate(_delegate = new TurnGameControllerDelegate(this));
    }

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // cast our context
        _ctx = (ToyBoxContext)super._ctx;
    }

    // documentation inherited
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        _panel = new AtlantiPanel((ToyBoxContext)ctx, this);
        return _panel;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _atlobj = (AtlantiObject)plobj;

        // find out what our index is and use that as our piecen color
        _selfIndex = ListUtil.indexOf(_atlobj.players, _ctx.getUsername());
        if (_selfIndex != -1) {
            // use our player index as the piecen color directly
            _panel.board.setNewPiecenColor(_selfIndex);
        }

        // wire up our board to be notified when our object state changes and
        // configure it with its starting state
        _atlobj.addListener(new DynamicListener(_panel.board));
        _panel.board.tilesChanged(_atlobj.tiles);
        _panel.board.piecensChanged(_atlobj.piecens);

        // if it's our turn, set the tile to be placed
        if (_delegate.isOurTurn()) {
            _panel.board.setTileToBePlaced(_atlobj.currentTile);
        }
    }

    // documentation inherited
    public void turnDidChange (Name turnHolder)
    {
        // if it's our turn, set the tile to be placed
        if (_ctx.getUsername().equals(turnHolder)) {
            _panel.board.setTileToBePlaced(_atlobj.currentTile);
        }
    }

    /**
     * Called by the {@link AtlantiBoardView} when a tile is placed by the
     * player.
     */
    public void tilePlaced (AtlantiTile tile)
    {
        // the user placed the tile into a valid location. grab the placed tile
        // from the board and submit it to the server
        _atlobj.manager.invoke("placeTile", tile);

        // if we have no piecens to place or if there are no unclaimed features
        // on the placed tile, we immediately disable piecen placement in the
        // board and expect that the server will end our turn
        int pcount = TileUtil.countPiecens(_atlobj.piecens, _selfIndex);
        if (pcount >= PIECENS_PER_PLAYER || !tile.hasUnclaimedFeature()) {
            _panel.board.cancelPiecenPlacement();
            _panel.noplace.setEnabled(false);

        } else {
            // otherwise, enable the noplace button
            _panel.noplace.setEnabled(true);
        }
    }

    /**
     * Called by the {@link AtlantiBoardView} when a piecen is placed by the
     * player on their previously placed tile.
     */
    public void piecenPlaced (Piecen piecen)
    {
        // the user placed a piecen on the tile. grab the piecen from the
        // placed tile and submit it to the server
        _atlobj.manager.invoke("placePiecen", piecen);

        // disable the noplace button
        _panel.noplace.setEnabled(false);
    }

    /**
     * Called by the {@link AtlantiBoardView} if a player chooses to place
     * nothing rather than a piecen.
     */
    public void placeNothing ()
    {
        // turn off piecen placement in the board
        _panel.board.cancelPiecenPlacement();

        // the user doesn't want to place anything this turn. send a place
        // nothing request to the server
        _atlobj.manager.invoke("placeNothing");

        // disable the noplace button
        _panel.noplace.setEnabled(false);
    }

    /**
     * Requests that we leave the game and return to the lobby.
     */
    public void backToLobby ()
    {
        _ctx.getLocationDirector().moveBack();
    }

    /** Provides access to client bits. */
    protected ToyBoxContext _ctx;

    /** Our turn game delegate. */
    protected TurnGameControllerDelegate _delegate;

    /** A reference to our game panel. */
    protected AtlantiPanel _panel;

    /** A reference to our game panel. */
    protected AtlantiObject _atlobj;

    /** Our player index or -1 if we're not a player. */
    protected int _selfIndex;
}
