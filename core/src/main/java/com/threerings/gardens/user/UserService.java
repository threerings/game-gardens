//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user;

import com.threerings.nexus.distrib.Address;
import com.threerings.nexus.distrib.NexusService;
import com.threerings.nexus.util.Callback;

import com.threerings.gardens.lobby.LobbyObject;

public interface UserService extends NexusService {

    /** Authenticates this session. Returns the address of the main lobby object.*/
    void authenticate (String sessionToken, Callback<Address<LobbyObject>> onAuthed);
}
