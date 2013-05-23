//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import react.Slot;

public class ReconnectPanel extends FlowPanel {

    public ReconnectPanel (ClientContext ctx) {
        Label status = new Label("Lost connection to server...");
        add(status);
        final Button reconnect = new Button("Reconnect");
        add(reconnect);
        final Connector conn = new Connector(ctx, status);
        conn.connecting.connect(new Slot<Boolean>() { public void onEmit (Boolean connecting) {
            reconnect.setEnabled(!connecting);
        }});
        reconnect.addClickHandler(new ClickHandler() { public void onClick (ClickEvent event) {
            conn.connect();
        }});
    }
}
