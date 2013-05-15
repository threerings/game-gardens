//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.user

import com.samskivert.depot.PersistenceContext
import com.samskivert.jdbc.ConnectionProvider
import com.threerings.user.depot.DepotUserRepository

import com.threerings.nexus.distrib.{Address, Nexus, NexusException, Singleton}
import com.threerings.nexus.util.Callback

import com.threerings.gardens.lobby.{LobbyManager, LobbyObject}

class UserManager (nexus :Nexus, conprov :ConnectionProvider, lobbyMgr :LobbyManager)
    extends Singleton with UserService {

  val obj = new UserObject(Factory_UserService.createDispatcher(this))

  /* ctor */ {
    nexus.registerSingleton(this)
    nexus.registerSingleton(obj, this);
  }

  override def authenticate (sessionToken :String, callback :Callback[Address[LobbyObject]]) {
    _urepo.loadUserBySession(sessionToken) match {
      case null => throw new NexusException("Invalid session token")
      case user =>
        lobbyMgr.userAuthed(User.register(user.userId, user.username))
        callback.onSuccess(Address.of(lobbyMgr.obj))
    }
  }

  private val _urepo = new DepotUserRepository(new PersistenceContext("userdb", conprov, null))
  private val _log = java.util.logging.Logger.getLogger("usermgr")
}
