//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.distrib;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.NexusObject;

/**
 * Creates {@link LobbyService} marshaller instances.
 */
public class Factory_LobbyService implements DService.Factory<LobbyService>
{
    @Override
    public DService<LobbyService> createService (NexusObject owner)
    {
        return new Marshaller(owner);
    }

    public static DService.Factory<LobbyService> createDispatcher (final LobbyService service)
    {
        return new DService.Factory<LobbyService>() {
            public DService<LobbyService> createService (NexusObject owner) {
                return new DService.Dispatcher<LobbyService>(owner) {
                    @Override public LobbyService get () {
                        return service;
                    }

                    @Override public Class<LobbyService> getServiceClass () {
                        return LobbyService.class;
                    }

                    @Override public void dispatchCall (short methodId, Object[] args) {
                        switch (methodId) {
                        default:
                            super.dispatchCall(methodId, args);
                        }
                    }
                };
            }
        };
    }

    protected static class Marshaller extends DService<LobbyService> implements LobbyService
    {
        public Marshaller (NexusObject owner) {
            super(owner);
        }
        @Override public LobbyService get () {
            return this;
        }
        @Override public Class<LobbyService> getServiceClass () {
            return LobbyService.class;
        }
    }
}
