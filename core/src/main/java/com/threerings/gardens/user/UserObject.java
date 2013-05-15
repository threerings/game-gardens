//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.NexusObject;
import com.threerings.nexus.distrib.Singleton;

public class UserObject extends NexusObject implements Singleton {

    /** Provides access to user services. */
    public final DService<UserService> svc;

    public UserObject (DService.Factory<UserService> svc) {
        this.svc = svc.createService(this);
    }
}
