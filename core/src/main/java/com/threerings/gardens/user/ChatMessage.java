//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user;

import com.threerings.nexus.io.Streamable;

/** Encapsulates a chat message. */
public class ChatMessage implements Streamable {

    public final String sender;
    public final String message;

    public ChatMessage (String sender, String message) {
        this.sender = sender;
        this.message = message;
    }
}
