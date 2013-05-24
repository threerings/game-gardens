//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link ChatMessage} and/or nested classes.
 */
public class Streamer_ChatMessage
    implements Streamer<ChatMessage>
{
    @Override
    public Class<?> getObjectClass () {
        return ChatMessage.class;
    }

    @Override
    public void writeObject (Streamable.Output out, ChatMessage obj) {
        writeObjectImpl(out, obj);
    }

    @Override
    public ChatMessage readObject (Streamable.Input in) {
        return new ChatMessage(
            in.readString(),
            in.readString()
        );
    }

    public static  void writeObjectImpl (Streamable.Output out, ChatMessage obj) {
        out.writeString(obj.sender);
        out.writeString(obj.message);
    }
}
