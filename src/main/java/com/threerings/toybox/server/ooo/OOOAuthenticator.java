//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2004-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;

import com.threerings.user.OOOUser;
import com.threerings.user.OOOUserManager;
import com.threerings.user.OOOUserRepository;
import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.presents.server.Authenticator;
import com.threerings.presents.server.net.AuthingConnection;

import com.threerings.crowd.data.TokenRing;

import com.threerings.toybox.server.ToyBoxConfig;

import static com.threerings.presents.data.AuthCodes.*;
import static com.threerings.toybox.Log.log;

/**
 * Delegates authentication to the OOO user manager.
 */
@Singleton
public class OOOAuthenticator extends Authenticator
{
    @Inject public OOOAuthenticator (ConnectionProvider conprov)
        throws PersistenceException
    {
        // we get our user manager configuration from the ocean config
        _usermgr = new OOOUserManager(ToyBoxConfig.config.getSubProperties("oooauth"), conprov);
        _authrep = _usermgr.getRepository();
    }

    // from abstract Authenticator
    protected void processAuthentication (AuthingConnection conn, AuthResponse rsp)
        throws Exception
    {
        // make sure we were properly initialized
        if (_authrep == null) {
            throw new AuthException(SERVER_ERROR);
        }
            
        // make sure they've sent valid credentials
        AuthRequest req = conn.getAuthRequest();
        if (!(req.getCredentials() instanceof UsernamePasswordCreds)) {
            log.warning("Invalid credentials: " + req);
            throw new AuthException(SERVER_ERROR);
        }
        UsernamePasswordCreds creds = (UsernamePasswordCreds)req.getCredentials();
        String username = creds.getUsername().toString();

        // load up their user account record
        OOOUser user = (OOOUser)_authrep.loadUser(username);
        if (user == null) {
            throw new AuthException(NO_SUCH_USER);
        }

        // now check their password
        if (!user.password.equals(creds.getPassword())) {
            throw new AuthException(INVALID_PASSWORD);
        }

        // configure their auth name using the canonical case from the OOOUser record
        conn.setAuthName(new Name(user.username));

        // configure a token ring for this user
        int tokens = 0;
        if (user.holdsToken(OOOUser.ADMIN)) {
            tokens |= TokenRing.ADMIN;
        }
        rsp.authdata = new TokenRing(tokens);

        log.info("User logged on", "user", username);
        rsp.getData().code = AuthResponseData.SUCCESS;
    }

    protected OOOUserRepository _authrep;
    protected OOOUserManager _usermgr;
}
