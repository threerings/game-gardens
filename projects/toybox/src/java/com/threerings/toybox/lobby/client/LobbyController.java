//
// $Id: LobbyController.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.lobby.client;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Handles the client side of the ToyBox match-making interface.
 */
public class LobbyController extends PlaceController
{
    // documentation inherited
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        // cast our references
        _ctx = (ToyBoxContext)ctx;
        _config = (LobbyConfig)config;

        super.init(ctx, config);
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        return new LobbyPanel(_ctx, _config);
    }

    protected ToyBoxContext _ctx;
    protected LobbyConfig _config;
}
