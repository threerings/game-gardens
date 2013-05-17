//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;

import com.threerings.nexus.distrib.Address;
import com.threerings.nexus.util.Callback;

import com.threerings.gardens.lobby.LobbyObject;
import com.threerings.gardens.user.UserObject;

public class Connector {

    public static void connect (final ClientContext ctx, final Label status) {
        abstract class CB<T> implements Callback<T> {
            @Override public void onFailure (Throwable cause) {
                status.setText(errorPre() + ": " + cause.getMessage());
            }
            protected abstract String errorPre ();
        }
        String hostname = Window.Location.getHostName();
        status.setText("Connecting to " + hostname + "...");

        // subscribe to the singleton UserObject on the specified host; this will trigger a
        // connection to that host
        Address<UserObject> addr = Address.create(hostname, UserObject.class);
        ctx.client().subscribe(addr, new CB<UserObject>() {
            public void onSuccess (UserObject obj) {
                // TODO: wire up onLost to show "reconnect" panel

                // TODO: show lobby or chat sidebar based on loc parameter
                status.setText("Connected. Entering lobby...");
                obj.svc.get().authenticate(ctx.authToken(), new CB<Address<LobbyObject>>() {
                    public void onSuccess (Address<LobbyObject> addr) {
                        status.setText("TODO: show lobby panel!");
                        // _ctx.setMainPanel(new LobbyPanel(ctx, addr));
                    }
                    protected String errorPre () { return "Failed to authorize"; }
                });
            }
            protected String errorPre () { return "Failed to connect"; }
        });
    }
}
