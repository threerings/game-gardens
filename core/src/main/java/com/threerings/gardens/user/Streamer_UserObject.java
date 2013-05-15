//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles the streaming of {@link UserObject} and/or nested classes.
 */
public class Streamer_UserObject
    implements Streamer<UserObject>
{
    @Override
    public Class<?> getObjectClass () {
        return UserObject.class;
    }

    @Override
    public void writeObject (Streamable.Output out, UserObject obj) {
        writeObjectImpl(out, obj);
        obj.writeContents(out);
    }

    @Override
    public UserObject readObject (Streamable.Input in) {
        UserObject obj = new UserObject(
            in.<UserService>readService()
        );
        obj.readContents(in);
        return obj;
    }

    public static  void writeObjectImpl (Streamable.Output out, UserObject obj) {
        out.writeService(obj.svc);
    }
}
