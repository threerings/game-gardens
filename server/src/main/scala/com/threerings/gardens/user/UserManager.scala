//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user

import react.RFuture

import com.samskivert.depot.PersistenceContext
import com.samskivert.jdbc.ConnectionProvider
import com.threerings.user.depot.DepotUserRepository

import com.threerings.nexus.distrib.{Address, Nexus, NexusException, Singleton}

import com.threerings.gardens.lobby.{LobbyManager, LobbyObject}

class UserManager (nexus :Nexus, conprov :ConnectionProvider, lobbyMgr :LobbyManager)
    extends Singleton with UserService {

  val obj = new UserObject(Factory_UserService.createDispatcher(this))

  /* ctor */ {
    nexus.registerSingleton(this)
    nexus.registerSingleton(obj, this);
  }

  override def authenticate (sessionToken :String) = {
    // TODO: if sessionToken is null, auth user as guest...
    val user = _urepo.loadUserBySession(sessionToken)
    NexusException.require(user != null, "Invalid session token")
    lobbyMgr.userAuthed(User.register(user.userId, user.username))
    RFuture.success(Address.of(lobbyMgr.obj))
  }

  private val _urepo = new DepotUserRepository(new PersistenceContext("userdb", conprov, null))
  private val _log = java.util.logging.Logger.getLogger("usermgr")
}
