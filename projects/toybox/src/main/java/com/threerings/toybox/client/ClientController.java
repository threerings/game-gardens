//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.toybox.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;

import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.Log.log;

/**
 * Responsible for top-level control of the client user interface.
 */
public class ClientController extends Controller
{
    /**
     * Creates a new client controller. The controller will set everything
     * up in preparation for logging on.
     */
    public ClientController (ToyBoxContext ctx, ToyBoxClient client)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _client = client;

        // we want to know about logon/logoff
        _ctx.getClient().addClientObserver(new ClientAdapter() {
            @Override
            public void clientDidLogoff (Client client) {
                _client.setMainPanel(_logonPanel);
            }
        });

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx, _client);
        _client.setMainPanel(_logonPanel);
    }

    /**
     * Returns a reference to the panel used to logon.
     */
    public LogonPanel getLogonPanel ()
    {
        return _logonPanel;
    }

    // documentation inherited
    @Override
    public boolean handleAction (ActionEvent action)
    {
	String cmd = action.getActionCommand();

        if (cmd.equals("logoff")) {
            // request that we logoff
            _ctx.getClient().logoff(true);
            return true;
        }

        log.info("Unhandled action: " + action);
        return false;
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxClient _client;

    // our panels
    protected LogonPanel _logonPanel;
}
