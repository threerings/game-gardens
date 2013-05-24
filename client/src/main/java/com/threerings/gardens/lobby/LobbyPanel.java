//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

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

import react.Connection;
import react.Slot;

import com.threerings.nexus.distrib.DMap;

import com.threerings.gwt.ui.EnterClickAdapter;
import com.threerings.gwt.ui.Widgets;

import com.threerings.gardens.client.ClientContext;
import com.threerings.gardens.user.ChatMessage;

public class LobbyPanel extends Composite {

    public LobbyPanel (ClientContext ctx, LobbyObject obj) {
        _ctx = ctx;
        _obj = obj;

        initWidget(_binder.createAndBindUi(this));

        // wire up the occupants list display
        new MapViewer<Integer,String>(_people) {
            protected Widget createView (Integer id, String name) {
                return Widgets.newLabel(name);
            }
        }.connect(_obj.occupants);

        // wire up the pending tables display and create button
        new MapViewer<Integer,LobbyObject.Table>(_penders) {
            protected Widget createView (Integer id, LobbyObject.Table table) {
                return new TableView(table);
            }
        }.connect(_obj.tables);

        _newGame.addClickHandler(new ClickHandler() {
            public void onClick (ClickEvent event) {
                _obj.svc.get().createTable("test", new GameConfig(), 2);
            }
        });

        // wire up the chat bits
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

    protected class TableView extends FlowPanel {
        public final LobbyObject.Table table;

        public TableView (LobbyObject.Table tbl) {
            this.table = tbl;

            add(Widgets.newLabel(table.gameName));
            // TODO: display game config
            _seats = new Button[table.seats];
            for (int ii = 0; ii < _seats.length; ii++) {
                final int seat = ii;
                _seats[ii] = new Button("Join");
                _seats[ii].addClickHandler(new ClickHandler() {
                    public void onClick (ClickEvent event) {
                        // TODO: make this leave rather than take if we're sitting here
                        _obj.svc.get().takeSeat(table.id, seat);
                    }
                });
            }
            add(Widgets.newRow(_seats));

            _conn = _obj.sitters.connectNotify(new DMap.Listener<LobbyObject.Seat,Integer>() {
                public void onPut (LobbyObject.Seat seat, Integer sitterId) {
                    if (seat.tableId == table.id) {
                        Button btn = _seats[seat.seat];
                        // TODO: if sitterId is us, make button say "Leave" and be enabled
                        btn.setText(_obj.occupants.get(sitterId));
                        btn.setEnabled(false);
                    }
                }
                public void onRemove (LobbyObject.Seat seat) {
                    if (seat.tableId == table.id) {
                        Button btn = _seats[seat.seat];
                        btn.setText("Join");
                        btn.setEnabled(true);
                    }
                }
            });
        }

        protected void onUnload () {
            super.onUnload();
            _conn.disconnect();
        }

        protected final Button[] _seats;
        protected final Connection _conn;
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
