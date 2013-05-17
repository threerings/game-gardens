//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.nexus.client.GWTClient;
import com.threerings.nexus.client.NexusClient;

import com.threerings.gardens.distrib.GardensSerializer;

/**
 * The main entry point for the GWT/HTML5 client.
 */
public class GardensEntryPoint implements EntryPoint {

    @Override public void onModuleLoad () {
        ClientContext ctx = new ClientContext() {
            public NexusClient client () {
                return _client;
            }
            public String authToken () {
                return Cookies.getCookie("id_");
            }
            public void setMainPanel (Widget main) {
                if (_main != null) {
                    RootPanel.get(CLIENT_DIV).remove(_main);
                }
                RootPanel.get(CLIENT_DIV).add(_main = main);
            }
            protected NexusClient _client = GWTClient.create(
                8080, /* TODO: get from deployment.properties */
                new GardensSerializer());
            protected Widget _main;
        };

        // if we're logged in, swap out the login UI for the logout UI
        String auth = ctx.authToken(), username = Cookies.getCookie("nm_");
        if (auth != null && username != null) {
            Document.get().getElementById("login").getStyle().setDisplay(Style.Display.NONE);
            Document.get().getElementById("userinfo").getStyle().clearDisplay();
            Document.get().getElementById("username").setInnerText(username);
        }

        Label status = new Label();
        ctx.setMainPanel(status);
        Connector.connect(ctx, status);
    }

    protected static final String CLIENT_DIV = "client";
}
