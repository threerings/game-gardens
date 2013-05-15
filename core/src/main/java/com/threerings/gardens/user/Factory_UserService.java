//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user;

import com.threerings.nexus.distrib.Address;
import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.NexusObject;
import com.threerings.nexus.util.Callback;

import com.threerings.gardens.lobby.LobbyObject;

/**
 * Creates {@link UserService} marshaller instances.
 */
public class Factory_UserService implements DService.Factory<UserService>
{
    @Override
    public DService<UserService> createService (NexusObject owner)
    {
        return new Marshaller(owner);
    }

    public static DService.Factory<UserService> createDispatcher (final UserService service)
    {
        return new DService.Factory<UserService>() {
            public DService<UserService> createService (NexusObject owner) {
                return new DService.Dispatcher<UserService>(owner) {
                    @Override public UserService get () {
                        return service;
                    }

                    @Override public Class<UserService> getServiceClass () {
                        return UserService.class;
                    }

                    @Override public void dispatchCall (short methodId, Object[] args) {
                        switch (methodId) {
                        case 1:
                            service.authenticate(
                                this.<String>cast(args[0]),
                                this.<Callback<Address<LobbyObject>>>cast(args[1]));
                            break;
                        default:
                            super.dispatchCall(methodId, args);
                        }
                    }
                };
            }
        };
    }

    protected static class Marshaller extends DService<UserService> implements UserService
    {
        public Marshaller (NexusObject owner) {
            super(owner);
        }
        @Override public UserService get () {
            return this;
        }
        @Override public Class<UserService> getServiceClass () {
            return UserService.class;
        }
        @Override public void authenticate (String sessionToken, Callback<Address<LobbyObject>> onAuthed) {
            postCall((short)1, sessionToken, onAuthed);
        }
    }
}
