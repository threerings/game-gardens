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

package com.threerings.toybox.lobby.server;

import java.util.logging.Level;

import com.threerings.presents.server.InvocationException;

import com.threerings.parlor.data.Table;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.server.TableManager;

import com.threerings.toybox.server.ToyBoxManager;

import static com.threerings.toybox.lobby.Log.log;

/**
 * Customizes the normal table manager with ToyBox specific requirements.
 */
public class ToyBoxTableManager extends TableManager
{
    public ToyBoxTableManager (ToyBoxManager toymgr, LobbyManager lmgr)
    {
        super(lmgr.getPlaceObject());
        _toymgr = toymgr;
        _lmgr = lmgr;
    }

    // documentation inherited
    protected int createGame (Table table)
        throws InvocationException
    {
        // fill the players array into the game config
        table.config.players = table.getPlayers();

        // pass the buck to the toybox manager to create the game
        GameObject gobj = _toymgr.createGame(_lmgr.getGame(), table.config);

        // tell the table manager about this game
        gameCreated(table, gobj);

        return gobj.getOid();
    }

    /** The toybox manager with whom we operate. */
    protected ToyBoxManager _toymgr;

    /** The lobby manager for which we're working. */
    protected LobbyManager _lmgr;
}
