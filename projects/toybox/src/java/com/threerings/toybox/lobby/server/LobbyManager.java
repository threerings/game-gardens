//
// $Id: LobbyManager.java,v 1.1 2004/11/15 01:48:51 mdb Exp $

package com.threerings.toybox.lobby.server;

import com.threerings.crowd.server.PlaceManager;

import com.threerings.toybox.lobby.data.LobbyObject;

/**
 * Takes care of the server side of a particular lobby.
 */
public class LobbyManager extends PlaceManager
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return LobbyObject.class;
    }
}
