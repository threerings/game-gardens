//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.gamegardens.com/code/
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

package com.threerings.toybox.lobby.client;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
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
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // let the toybox director know that we're in
        _ctx.getToyBoxDirector().enteredLobby(_config);

        // have the toybox director download this game's jar files
        _ctx.getToyBoxDirector().resolveResources(
            // TODO: wire up a fancy display in the lobby panel
            _config.getGameDefinition(), null);
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        return new LobbyPanel(_ctx, _config);
    }

    protected ToyBoxContext _ctx;
    protected LobbyConfig _config;
}
