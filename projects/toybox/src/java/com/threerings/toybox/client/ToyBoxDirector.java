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

package com.threerings.toybox.client;

import com.samskivert.util.StringUtil;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.client.LocationAdapter;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.Log.log;

/**
 * Handles the client side of the ToyBox services.
 */
public class ToyBoxDirector extends BasicDirector
{
    public ToyBoxDirector (ToyBoxContext ctx)
    {
        super(ctx);
        _ctx = ctx;
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // determine which lobby we are to enter...
        final String ident = System.getProperty("game_ident");
        if (StringUtil.blank(ident)) {
            log.warning("Missing 'game_ident' system property.");
            return;
        }

        // ...and issue a request to do so
        ToyBoxService.ResultListener rl = new ToyBoxService.ResultListener() {
            public void requestProcessed (Object result) {
                enterLobby(ident, (Integer)result);
            }

            public void requestFailed (String cause) {
                // TODO: report this error graphically
                log.warning("Failed to get lobby oid [game=" + ident +
                            ", error=" + cause + "].");
            }
        };
        log.fine("Requesting lobby oid [ident=" + ident + "].");
        _toysvc.getLobbyOid(client, ident, rl);
    }

    /** Helper method for entering a lobby and reporting any failure. */
    protected void enterLobby (final String ident, int lobbyOid)
    {
        log.fine("Entering lobby [ident=" + ident + ", oid=" + lobbyOid + "].");

        // wire up a location observer that can detect if we fail to make
        // it into our requested lobby
        LocationAdapter obs = new LocationAdapter() {
            public void locationDidChange (PlaceObject place) {
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
            public void locationChangeFailed (int placeId, String reason) {
                // TODO: report this error graphically
                log.warning("Failed to enter lobby [game=" + ident +
                            ", error=" + reason + "].");
                _ctx.getLocationDirector().removeLocationObserver(this);
            }
        };
        _ctx.getLocationDirector().addLocationObserver(obs);
        _ctx.getLocationDirector().moveTo(lobbyOid);

    }
    // documentation inherited
    protected void fetchServices (Client client)
    {
        super.fetchServices(client);
        _toysvc = (ToyBoxService)client.requireService(ToyBoxService.class);
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxService _toysvc;
}
