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

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.data.Table;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.game.server.GameManagerDelegate;
import com.threerings.parlor.server.TableManager;

import com.threerings.toybox.server.ToyBoxServer;

import static com.threerings.toybox.lobby.Log.log;

/**
 * Customizes the normal table manager with ToyBox specific requirements.
 */
public class ToyBoxTableManager extends TableManager
{
    public ToyBoxTableManager (LobbyManager lmgr)
    {
        super(lmgr);
        _lmgr = lmgr;
    }

    // documentation inherited
    protected void createGame (final Table table)
        throws InvocationException
    {
        // fill the players array into the game config
        table.config.players = table.getPlayers();

        // TODO: various complicated bits to pass this request off to the
        // standalone game server
        PlaceRegistry.CreationObserver obs =
            new PlaceRegistry.CreationObserver() {
            public void placeCreated (PlaceObject plobj, PlaceManager pmgr) {
                // add a delegate that will record the game's playtime
                // upon completion
                pmgr.addDelegate(new GameManagerDelegate((GameManager)pmgr) {
                    public void gameWillStart () {
                        _started = System.currentTimeMillis();
                    }
                    public void gameDidEnd () {
                        long playtime = System.currentTimeMillis() - _started;
                        ToyBoxServer.toymgr.recordPlaytime(
                            _lmgr.getGame(), playtime);
                    }
                    protected long _started;
                });
                gameCreated(table, plobj);
            }
        };
        try {
            ToyBoxServer.plreg.createPlace(table.config, obs);
        } catch (InstantiationException ie) {
            log.log(Level.WARNING, "Failed to create manager for game " +
                    "[config=" + table.config + "]", ie);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /** The lobby manager for which we're working. */
    protected LobbyManager _lmgr;
}
