//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

import com.threerings.nexus.distrib.DMap;
import com.threerings.nexus.distrib.DService;
import com.threerings.nexus.distrib.DSignal;
import com.threerings.nexus.distrib.NexusObject;
import com.threerings.nexus.io.Streamable;

import com.threerings.gardens.user.ChatMessage;

/** Contains the distributed state of the lobby. */
public class LobbyObject extends NexusObject {

    /** Represents a table currently being match-made. */
    public static class Table implements Streamable {
        public final int id;
        public final int seats;
        public final String gameName;
        public final GameConfig config;

        public Table (int id, int seats, String gameName, GameConfig config) {
            this.id = id;
            this.seats = seats;
            this.gameName = gameName;
            this.config = config;
        }
    }

    /** Represents a game currently in progress. */
    public static class Game implements Streamable {
        public final int id;
        public final String gameName;
        public final GameConfig config;
        public String[] players;

        public Game (int id, String gameName, GameConfig config, String[] players) {
            this.id = id;
            this.gameName = gameName;
            this.config = config;
            this.players = players;
        }
    }

    /** Represents a seat at a particular table. */
    public static class Seat implements Streamable {
        public final int tableId;
        public final int seat;

        public Seat (int tableId, int seat) {
            this.tableId = tableId;
            this.seat = seat;
        }

        @Override public int hashCode () { return tableId * 100 + seat; }
        @Override public boolean equals (Object other) {
            return other instanceof Seat && ((Seat)other).tableId == tableId &&
                ((Seat)other).seat == seat;
        }
        @Override public String toString () { return tableId + "@" + seat; }
    }

    /** Provides access to lobby services. */
    public final DService<LobbyService> svc;

    /** Emitted when someone sends a chat event. */
    public final DSignal<ChatMessage> chat = DSignal.create(this);

    /** The occupants of the lobby as {@code id -> username}. */
    public final DMap<Integer,String> occupants = DMap.create(this);

    /** The tables currently being match-made, mapped by id. */
    public final DMap<Integer,Table> tables = DMap.create(this);

    /** The games currently in-progress, mapped by id. */
    public final DMap<Integer,Game> games = DMap.create(this);

    /** Contains a mapping from {table,seat} to occupant user id. */
    public final DMap<Seat,Integer> sitters = DMap.create(this);

    public LobbyObject (DService.Factory<LobbyService> svc) {
        this.svc = svc.createService(this);
    }
}
