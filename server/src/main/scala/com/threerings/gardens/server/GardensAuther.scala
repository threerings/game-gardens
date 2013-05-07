//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server

import com.google.inject.{Inject, Singleton}

import com.samskivert.jdbc.ConnectionProvider

import com.threerings.user.OOOUser
import com.threerings.user.depot.DepotUserManager
import com.threerings.util.Name

import com.threerings.presents.data.AuthCodes._
import com.threerings.presents.net.UsernamePasswordCreds
import com.threerings.presents.net.{AuthResponse, AuthResponseData}
import com.threerings.presents.server.Authenticator
import com.threerings.presents.server.net.AuthingConnection

import com.threerings.crowd.data.TokenRing

/**
 * Delegates authentication to the OOO user manager.
 */
@Singleton class GardensAuther @Inject() (config :GardensConfig, conprov :ConnectionProvider)
    extends Authenticator {

  override protected def processAuthentication (conn :AuthingConnection, rsp :AuthResponse) {
    val req = conn.getAuthRequest
    // make sure they've sent valid credentials
    req.getCredentials match {
      case creds :UsernamePasswordCreds =>
        val username = creds.getUsername.toString

        // load up their user account record
        val user = _usermgr.getRepository.loadUser(username).asInstanceOf[OOOUser]
        if (user == null) {
          throw new Authenticator.AuthException(NO_SUCH_USER)
        }

        // now check their password
        if (!user.password.equals(creds.getPassword)) {
          throw new Authenticator.AuthException(INVALID_PASSWORD)
        }

        // configure their auth name using the canonical case from the OOOUser record
        conn.setAuthName(new Name(user.username))

        // configure a token ring for this user
        val tokens = if (user.holdsToken(OOOUser.ADMIN)) TokenRing.ADMIN else 0
        rsp.authdata = new TokenRing(tokens)

        _log.info(s"User logged on [user=$username]")
        rsp.getData().code = AuthResponseData.SUCCESS

      case _ =>
        _log.warning(s"Invalid credentials: $req")
        throw new Authenticator.AuthException(SERVER_ERROR)
    }
  }

  protected val _usermgr = new DepotUserManager(config.webConfig, conprov)
  protected val _log = java.util.logging.Logger.getLogger("gardens")
}
