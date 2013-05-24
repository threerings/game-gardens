//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;

import react.Function;
import react.RFuture;
import react.Slot;
import react.UnitSlot;
import react.Value;

import com.threerings.nexus.distrib.Address;

import com.threerings.gardens.lobby.LobbyObject;
import com.threerings.gardens.lobby.LobbyPanel;
import com.threerings.gardens.user.UserObject;

public class Connector {

    public final Value<Boolean> connecting = Value.create(false);

    public Connector (ClientContext ctx, Label status) {
        _ctx = ctx;
        _status = status;
    }

    public void connect () {
        connecting.update(true);
        _status.setText("Connecting to server...");

        _ctx.client().<UserObject>subscriber().
            subscribe(Address.create(Window.Location.getHostName(), UserObject.class)).
            flatMap(new Function<UserObject,RFuture<Address<LobbyObject>>>() {
                public RFuture<Address<LobbyObject>> apply (UserObject obj) {
                    // if we lose connection to the server, drop back to a "reconect" panel
                    obj.onLost.connect(new UnitSlot() { public void onEmit () {
                        _ctx.setMainPanel(new ReconnectPanel(_ctx));
                    }});
                    // TODO: show lobby or chat sidebar based on loc parameter
                    _status.setText("Authenticating...");
                    return obj.svc.get().authenticate(_ctx.authToken());
                }
            }).
            flatMap(_ctx.client().<LobbyObject>subscriber()).
            onSuccess(new Slot<LobbyObject>() { public void onEmit (LobbyObject obj) {
                _ctx.setMainPanel(new LobbyPanel(_ctx, obj));
            }}).
            onFailure(onFailure);
    }

    // TODO: if we get invalid session token, clear our cookie, log in as a guest, and tell the
    // containing page to clear out the "you are logged in" display
    protected final Slot<Throwable> onFailure = new Slot<Throwable>() {
        public void onEmit (Throwable cause) {
            _status.setText("Error: " + cause.getMessage());
            connecting.update(false);
        }
    };

    protected final ClientContext _ctx;
    protected final Label _status;
}
