//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby

import com.threerings.nexus.distrib.{Nexus, Singleton}

import com.threerings.gardens.user.{ChatMessage, User}

class LobbyManager (nexus :Nexus) extends Singleton with LobbyService {
  import LobbyObject.Table

  val obj = new LobbyObject(Factory_LobbyService.createDispatcher(this))

  /* ctor */ {
    nexus.registerSingleton(this)
    nexus.register(obj, this)
  }

  def userAuthed (user :User) {
    obj.occupants.put(user.id, user.name)
  }

  override def createTable (gameIdent :String, config :GameConfig, seats :Int) {
    val user = User.require
    val tableId = _tableIdGen.nextId()
    val table = new Table(tableId, seats, "TODO", config)
    obj.tables.put(tableId, table)
    // if this table has seats, take the first seat
    if (seats > 0) takeSeat(tableId, 0)
    // otherwise it's a party game so start it immediately
    else startGame(table)
  }

  override def takeSeat (tableId :Int, seat :Int) {
    val user = User.require
    obj.tables.get(tableId) match {
      case null => _log.warning(s"No table for takeSeat [user=${user.id}, table=$tableId]")
      case table =>
        val key = LobbyObject.sittersKey(tableId, seat)
        if (!obj.sitters.containsKey(key)) obj.sitters.put(key, user.id)
        // if this game is now ready to go, start it
        if (seatKeys(table) forall(k => obj.sitters.containsKey(k))) startGame(table)
    }
  }

  override def leaveSeat (tableId :Int) {
    val user = User.require
    obj.tables.get(tableId) match {
      case null => _log.warning(s"No table for leaveSeat [user=${user.id}, table=$tableId]")
      case table =>
        // we have to inspect each seat at this table to find the one we're sitting at
        for (key <- seatKeys(table)) {
          if (obj.sitters.get(key) == user.id) {
            obj.sitters.remove(key)
          }
        }
    }
  }

  override def sendChat (message :String) {
    val user = User.require
    // TODO: check things?
    obj.chat.emit(new ChatMessage(user.name, message))
  }

  protected def seatKeys (table :Table) =
    0 until table.seats map(s => LobbyObject.sittersKey(table.id, s))

  protected def startGame (table :Table) {
    // TODO
    _log.info(s"TODO: start game ${table.id}")
  }

  private val _tableIdGen = new IdGen
  private val _gameIdGen = new IdGen
  private val _log = java.util.logging.Logger.getLogger("lobmgr")

  private class IdGen {
    def nextId () = {
      val id = _nextId
      _nextId += 1
      id
    }
    private var _nextId = 1
  }
}
