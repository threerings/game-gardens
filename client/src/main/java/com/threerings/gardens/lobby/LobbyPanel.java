//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import react.Slot;

import com.threerings.gwt.ui.EnterClickAdapter;
import com.threerings.gwt.ui.Widgets;

import com.threerings.nexus.distrib.DMap;

import com.threerings.gardens.client.ClientContext;
import com.threerings.gardens.user.ChatMessage;

public class LobbyPanel extends Composite {

    public LobbyPanel (ClientContext ctx, LobbyObject obj) {
        _ctx = ctx;
        _obj = obj;

        initWidget(_binder.createAndBindUi(this));

        _obj.chat.connect(new Slot<ChatMessage>() {
            public void onEmit (ChatMessage msg) {
                if (msg.sender == null) appendLine(msg.message); // from the server
                else appendLine("<" + msg.sender + "> " + msg.message);
            }
        });

        ClickHandler onSend = new ClickHandler() {
            public void onClick (ClickEvent event) {
                String message = _entry.getText().trim();
                if (message.length() > 0) {
                    _obj.svc.get().sendChat(message);
                    _entry.setText("");
                }
            }
        };
        _send.addClickHandler(onSend);
        EnterClickAdapter.bind(_entry, onSend);

        DMap.Listener<Integer,String> occupanter = new DMap.Listener<Integer,String>() {
            @Override public void onPut (Integer id, String name) {
                Widget nlabel = Widgets.newLabel(name);
                _widgets.put(id, nlabel);
                _people.add(nlabel); // TODO: sorted
            }
            @Override public void onRemove (Integer id) {
                Widget nlabel = _widgets.remove(id);
                if (nlabel != null) _people.remove(nlabel);
            }
            protected Map<Integer,Widget> _widgets = new HashMap<Integer,Widget>();
        };
        _obj.occupants.connect(occupanter);
        for (Map.Entry<Integer,String> entry : _obj.occupants.entrySet()) {
            occupanter.onPut(entry.getKey(), entry.getValue());
        }
    }

    protected void feedback (String message) {
        appendLine(message);
    }

    protected void appendLine (String line) {
        _messages.add(Widgets.newLabel(line));
    }

    protected Slot<Throwable> reportFailure (final String errpre) {
        return new Slot<Throwable>() { public void onEmit (Throwable cause) {
            feedback(errpre + ": " + cause.getMessage());
        }};
    }

    protected interface Styles extends CssResource {
    }
    protected @UiField Styles _styles;

    protected final ClientContext _ctx;
    protected final LobbyObject _obj;

    protected @UiField FlowPanel _penders;
    protected @UiField Button _newGame;

    protected @UiField FlowPanel _runners;
    protected @UiField FlowPanel _people;

    protected @UiField FlowPanel _messages;
    protected @UiField TextBox _entry;
    protected @UiField Button _send;

    protected interface Binder extends UiBinder<Widget, LobbyPanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
}
