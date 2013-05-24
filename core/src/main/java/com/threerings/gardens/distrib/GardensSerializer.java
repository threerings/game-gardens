//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.distrib;

import com.threerings.nexus.io.AbstractSerializer;

import com.threerings.gardens.lobby.*;
import com.threerings.gardens.user.*;

/**
 * Contains the mapping for all streamable classes used by the client. (Will some day be
 * auto-generated.)
 */
public class GardensSerializer extends AbstractSerializer {

    public GardensSerializer () {
        mapStreamer(new Streamer_GameConfig());
        mapStreamer(new Streamer_LobbyObject());
        mapStreamer(new Streamer_LobbyObject.Table());
        mapStreamer(new Streamer_LobbyObject.Game());
        mapStreamer(new Streamer_LobbyObject.Seat());
        mapStreamer(new Streamer_UserObject());
        mapStreamer(new Streamer_ChatMessage());
        mapService(new Factory_LobbyService(), LobbyService.class);
        mapService(new Factory_UserService(), UserService.class);
    }
}
