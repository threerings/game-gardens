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

package com.threerings.toybox.server.ooo;

import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.Invoker;
import com.threerings.util.Name;

import com.threerings.user.OOOUser;
import com.threerings.user.OOOUserManager;
import com.threerings.user.OOOUserRepository;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.net.AuthingConnection;

import com.threerings.toybox.server.ToyBoxConfig;
import com.threerings.toybox.server.ToyBoxServer;

import static com.threerings.presents.data.AuthCodes.*;
import static com.threerings.toybox.Log.log;

/**
 * Delegates authentication to the OOO user manager.
 */
public class OOOAuthenticator extends Authenticator
{
    public OOOAuthenticator ()
    {
        try {
            // we get our user manager configuration from the ocean config
            _usermgr = new OOOUserManager(
                ToyBoxConfig.config.getSubProperties("oooauth"),
                ToyBoxServer.conprov);
            _authrep = (OOOUserRepository)_usermgr.getRepository();
        } catch (PersistenceException pe) {
            log.log(Level.WARNING, "Failed to initialize OOO authenticator. " +
                    "Users will be unable to log in.", pe);
        }
    }

    // documentation inherited
    public void authenticateConnection (final AuthingConnection conn)
    {
        // fire up an invoker unit that will load the user object just to
        // make sure they exist
        String name = "auth:" + conn.getAuthRequest().getCredentials();
        ToyBoxServer.invoker.postUnit(new Invoker.Unit(name) {
            public boolean invoke () {
                processAuthentication(conn);
                return false;
            }
        });
    }

    /**
     * Here we do the actual authentication processing while running
     * happily on the invoker thread.
     */
    protected void processAuthentication (AuthingConnection conn)
    {
        AuthRequest req = conn.getAuthRequest();
        AuthResponseData rdata = new AuthResponseData();
        AuthResponse rsp = new AuthResponse(rdata);

        try {
            // make sure we were properly initialized
            if (_authrep == null) {
                rdata.code = SERVER_ERROR;
                return;
            }

            // make sure they've sent valid credentials
            if (!(req.getCredentials() instanceof UsernamePasswordCreds)) {
                rdata.code = SERVER_ERROR;
                log.warning("Invalid credentials: " + req);
                // note that the finally block will be executed and
                // communicate our auth response back to the conmgr
                return;
            }
            UsernamePasswordCreds creds = (UsernamePasswordCreds)
                req.getCredentials();
            String username = creds.getUsername().toString();

            // load up their user account record
            OOOUser user = (OOOUser)_authrep.loadUser(username);
            if (user == null) {
                rdata.code = NO_SUCH_USER;
                return;
            }

            // now check their password
            if (!user.password.equals(creds.getPassword())) {
                rdata.code = INVALID_PASSWORD;
                return;
            }

            log.info("User logged on [user=" + username + "].");
            rdata.code = AuthResponseData.SUCCESS;

        } catch (PersistenceException pe) {
            log.log(Level.WARNING, "Error authenticating user " +
                    "[areq=" + req + "].", pe);
            rdata.code = SERVER_ERROR;

        } finally {
            // let the powers that be know that we're done authenticating
            connectionWasAuthenticated(conn, rsp);
        }
    }

    protected OOOUserRepository _authrep;
    protected OOOUserManager _usermgr;
}
