//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link GameConfig} and/or nested classes.
 */
public class Streamer_GameConfig
    implements Streamer<GameConfig>
{
    @Override
    public Class<?> getObjectClass () {
        return GameConfig.class;
    }

    @Override
    public void writeObject (Streamable.Output out, GameConfig obj) {
        writeObjectImpl(out, obj);
    }

    @Override
    public GameConfig readObject (Streamable.Input in) {
        return new GameConfig(
        );
    }

    public static  void writeObjectImpl (Streamable.Output out, GameConfig obj) {
    }
}
