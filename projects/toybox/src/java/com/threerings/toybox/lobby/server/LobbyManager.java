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

import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.SetAdapter;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.server.TableManager;
import com.threerings.parlor.server.TableManagerProvider;

import com.threerings.toybox.data.GameDefinition;
import com.threerings.toybox.data.TableMatchConfig;
import com.threerings.toybox.server.ToyBoxManager;
import com.threerings.toybox.server.persist.Game;

import com.threerings.toybox.lobby.data.LobbyConfig;
import com.threerings.toybox.lobby.data.LobbyObject;

import static com.threerings.toybox.lobby.Log.log;

/**
 * Takes care of the server side of a particular lobby.
 */
public class LobbyManager extends PlaceManager
    implements TableManagerProvider
{
    // documentation inherited from interface
    public TableManager getTableManager ()
    {
        return _tablemgr;
    }

    /**
     * Provides this lobby manager with a reference to its game and the ToyBox
     * manager with whom it should work.
     */
    public void init (ToyBoxManager toymgr, Game game)
    {
        _toymgr = toymgr;
        _game = game;

        // this happens after we've started up so we configure our lobby
        // object with the game name now
        _lobobj.setName(_game.name);

        // we don't have a game reference the first time our idle unload
        // interval is scheduled, so cancel it if we discover that we're in
        // testing mode and don't ever want to unload
        if (game.gameId == -1) {
            cancelShutdowner();
        }

        // if we're using the table services to match-make, create a table
        // manager
        GameDefinition gdef = _lconfig.getGameDefinition();
        if (gdef.match instanceof TableMatchConfig) {
            _tablemgr = new ToyBoxTableManager(_toymgr, this);
        }
    }

    /**
     * Returns a reference to the game associated with this lobby.
     */
    public Game getGame ()
    {
        return _game;
    }

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        _lconfig = (LobbyConfig)_config;
    }

    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();

        _lobobj = (LobbyObject)_plobj;
        _lobobj.addListener(_emptyListener);
    }

    // documentation inherited
    protected void placeBecameEmpty ()
    {
        // we don't want to do the standard "became empty" processing
        // until all of our tables are also empty
        if (_lobobj.tableSet.size() == 0) {
            super.placeBecameEmpty();
        }
    }

    // documentation inherited
    protected long idleUnloadPeriod ()
    {
        // unload our lobbies very quickly after they become empty; unless
        // we're in testing mode in which case we want to never unload
        return (_game != null && _game.gameId == -1) ? 0L : 15 * 1000L;
    }

    // documentation inherited
    protected void didShutdown ()
    {
        super.didShutdown();

        // unregister with our toybox manager
        _toymgr.lobbyDidShutdown(_game);
    }

    // documentation inherited
    protected PlaceObject createPlaceObject ()
    {
        return new LobbyObject();
    }

    /** Listens for tables shutting down and reports us as empty if there
     * are no people in the lobby and our last table went away. */
    protected SetAdapter _emptyListener = new SetAdapter() {
        public void entryRemoved (EntryRemovedEvent event) {
            if (event.getName().equals(LobbyObject.TABLE_SET)) {
                if (_lobobj.tableSet.size() == 0 &&
                    _lobobj.occupants.size() == 0) {
                    placeBecameEmpty();
                }
            }
        }
    };

    /** The ToyBox manager with whom we operate. */
    protected ToyBoxManager _toymgr;

    /** The game record associated with our game. */
    protected Game _game;

    /** A casted reference to our lobby config. */
    protected LobbyConfig _lconfig;

    /** A casted reference to our lobby object. */
    protected LobbyObject _lobobj;

    /** Our table manager, which is only created if we're using tables to
     * match-make in this lobby. */
    protected TableManager _tablemgr;
}
