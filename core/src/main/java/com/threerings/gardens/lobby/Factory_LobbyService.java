//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.NexusObject;

import react.RFuture;

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

                    @Override public RFuture<?> dispatchCall (short methodId, Object[] args) {
                        RFuture<?> result = null;
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
                        case 4:
                            service.sendChat(
                                this.<String>cast(args[0]));
                            break;
                        default:
                            result = super.dispatchCall(methodId, args);
                        }
                        return result;
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
            postVoidCall((short)1, gameIdent, config, seats);
        }
        @Override public void takeSeat (int tableId, int seat) {
            postVoidCall((short)2, tableId, seat);
        }
        @Override public void leaveSeat (int tableId) {
            postVoidCall((short)3, tableId);
        }
        @Override public void sendChat (String message) {
            postVoidCall((short)4, message);
        }
    }
}
