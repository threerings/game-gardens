//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

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
                        case 1:
                            service.createTable(
                                this.<String>cast(args[0]),
                                this.<GameConfig>cast(args[1]),
                                this.<Integer>cast(args[2]));
                            break;
                        case 2:
                            service.takeSeat(
                                this.<Integer>cast(args[0]),
                                this.<Integer>cast(args[1]));
                            break;
                        case 3:
                            service.leaveSeat(
                                this.<Integer>cast(args[0]));
                            break;
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
        @Override public void createTable (String gameIdent, GameConfig config, int seats) {
            postCall((short)1, gameIdent, config, seats);
        }
        @Override public void takeSeat (int tableId, int seat) {
            postCall((short)2, tableId, seat);
        }
        @Override public void leaveSeat (int tableId) {
            postCall((short)3, tableId);
        }
    }
}
