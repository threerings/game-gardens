//
// $Id$
//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
// http://www.gamegardens.com/code/
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
import com.threerings.presents.client.SessionObserver;

import com.threerings.crowd.data.BodyObject;

import com.threerings.toybox.util.ToyBoxContext;

import static com.threerings.toybox.Log.log;

/**
 * Responsible for top-level control of the client user interface.
 */
public class ClientController extends Controller
    implements SessionObserver
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
        _ctx.getClient().addClientObserver(this);

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx, _client);
        _client.setMainPanel(_logonPanel);
    }

    // documentation inherited
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

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        log.info("Client did logon [client=" + client + "].");

        // keep the body object around for stuff
        _body = (BodyObject)client.getClientObject();
    }

    // documentation inherited
    public void clientObjectDidChange (Client client)
    {
        // regrab our body object
        _body = (BodyObject)client.getClientObject();
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
        log.info("Client did logoff [client=" + client + "].");

        // reinstate the logon panel
        _client.setMainPanel(_logonPanel);
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxClient _client;
    protected BodyObject _body;

    // our panels
    protected LogonPanel _logonPanel;
}
