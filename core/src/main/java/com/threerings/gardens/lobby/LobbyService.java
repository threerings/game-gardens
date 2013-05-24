//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

import com.threerings.nexus.distrib.NexusService;

public interface LobbyService extends NexusService {

    /** Creates a new table for the specified game, with the specified config and number of seats.
     * If the table has zero seats (ie. it's a party game), the game is started immediately,
     * otherwise the creating player is seated in seat zero.
     */
    void createTable (String gameIdent, GameConfig config, int seats);

    /** Requests to sit at the specified seat in the specified table. If the request succeeds, the
     * client will see a mapping show up in the lobby's distributed state. If not, it won't.
     */
    void takeSeat (int tableId, int seat);

    /** Requests to leave the seat occupied by this client at the specified table. */
    void leaveSeat (int tableId);

    /** Requests that the specified message be sent as chat to this lobby. */
    void sendChat (String message);
}
