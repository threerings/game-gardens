//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.client;

import com.google.gwt.user.client.ui.Widget;

import com.threerings.nexus.client.NexusClient;

/** Provides access to "global" services. */
public interface ClientContext {

    /** Returns our Nexus client. */
    NexusClient client ();

    /** Returns our current auth token. */
    String authToken ();

    /** Replaces the main panel being displayed by the client. */
    void setMainPanel (Widget panel);
}
