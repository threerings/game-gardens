//
// $Id: ClientController.java,v 1.1 2004/11/15 22:37:36 mdb Exp $

package com.threerings.toybox.client;

import java.awt.event.ActionEvent;
import com.samskivert.swing.Controller;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.SessionObserver;

import com.threerings.crowd.data.BodyObject;

import com.threerings.toybox.Log;
import com.threerings.toybox.util.ToyBoxContext;

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
    public ClientController (ToyBoxContext ctx, ToyBoxFrame frame)
    {
        // we'll want to keep these around
        _ctx = ctx;
        _frame = frame;

        // we want to know about logon/logoff
        _ctx.getClient().addClientObserver(this);

        // create the logon panel and display it
        _logonPanel = new LogonPanel(_ctx);
        _frame.setPanel(_logonPanel);
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

        Log.info("Unhandled action: " + action);
        return false;
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
        Log.info("Client did logon [client=" + client + "].");

        // keep the body object around for stuff
        _body = (BodyObject)client.getClientObject();

        if (_body.location != -1) {
            // if we were already in a location, go there (this probably
            // means we are reconnecting to an existing session)
            _ctx.getLocationDirector().moveTo(_body.location);

        } else {
            // TODO
        }
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
        Log.info("Client did logoff [client=" + client + "].");

        // reinstate the logon panel
        _frame.setPanel(_logonPanel);
    }

    protected ToyBoxContext _ctx;
    protected ToyBoxFrame _frame;
    protected BodyObject _body;

    // our panels
    protected LogonPanel _logonPanel;
}
