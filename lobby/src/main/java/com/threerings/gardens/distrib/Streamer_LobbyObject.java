//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.distrib;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link LobbyObject} and/or nested classes.
 */
public class Streamer_LobbyObject
    implements Streamer<LobbyObject>
{
    /**
     * Handles the streaming of {@link LobbyObject.Table} instances.
     */
    public static class Table
        implements Streamer<LobbyObject.Table>
    {
        @Override
        public Class<?> getObjectClass () {
            return LobbyObject.Table.class;
        }

        @Override
        public void writeObject (Streamable.Output out, LobbyObject.Table obj) {
            writeObjectImpl(out, obj);
        }

        @Override
        public LobbyObject.Table readObject (Streamable.Input in) {
            return new LobbyObject.Table(
                in.readInt(),
                in.readInt(),
                in.readString(),
                in.<GameConfig>readValue()
            );
        }

        public static  void writeObjectImpl (Streamable.Output out, LobbyObject.Table obj) {
            out.writeInt(obj.id);
            out.writeInt(obj.seats);
            out.writeString(obj.gameName);
            out.writeValue(obj.config);
        }
    }

    /**
     * Handles the streaming of {@link LobbyObject.Game} instances.
     */
    public static class Game
        implements Streamer<LobbyObject.Game>
    {
        @Override
        public Class<?> getObjectClass () {
            return LobbyObject.Game.class;
        }

        @Override
        public void writeObject (Streamable.Output out, LobbyObject.Game obj) {
            writeObjectImpl(out, obj);
        }

        @Override
        public LobbyObject.Game readObject (Streamable.Input in) {
            return new LobbyObject.Game(
                in.readInt(),
                in.readString(),
                in.<GameConfig>readValue(),
                in.readStrings()
            );
        }

        public static  void writeObjectImpl (Streamable.Output out, LobbyObject.Game obj) {
            out.writeInt(obj.id);
            out.writeString(obj.gameName);
            out.writeValue(obj.config);
            out.writeStrings(obj.players);
        }
    }

    @Override
    public Class<?> getObjectClass () {
        return LobbyObject.class;
    }

    @Override
    public void writeObject (Streamable.Output out, LobbyObject obj) {
        writeObjectImpl(out, obj);
        obj.writeContents(out);
    }

    @Override
    public LobbyObject readObject (Streamable.Input in) {
        LobbyObject obj = new LobbyObject(
            in.<LobbyService>readService()
        );
        obj.readContents(in);
        return obj;
    }

    public static  void writeObjectImpl (Streamable.Output out, LobbyObject obj) {
        out.writeService(obj.svc);
    }
}
