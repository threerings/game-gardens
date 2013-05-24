//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby

import com.threerings.gardens.user.{ChatMessage, User}
import com.threerings.nexus.distrib.{Nexus, Singleton}
import react.Slot
import scala.collection.JavaConverters._

class LobbyManager (nexus :Nexus) extends Singleton with LobbyService {
  import LobbyObject.Table

  val obj = new LobbyObject(Factory_LobbyService.createDispatcher(this))

  /* ctor */ {
    nexus.registerSingleton(this)
    nexus.register(obj, this)
  }

  def userAuthed (user :User) {
    obj.occupants.put(user.id, user.name)
    user.onDisconnect.connect(new Slot[User] {
      def onEmit (user :User) = {
        forceStand(user)
        obj.occupants.remove(user.id)
      }
    })
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
    // if we're currently in another seat, note it (we do this rather than leaving our current seat
    // immediately because this allows us to switch seats in a just started table; if we left the
    // table first, the table would become empty and go away before we could sit in it)
    val seats = occupiedSeats(user)
    // now sit in the requested seat
    obj.tables.get(tableId) match {
      case null => _log.warning(s"No table for takeSeat [user=$user, table=$tableId]")
      case table =>
        val key = new LobbyObject.Seat(tableId, seat)
        if (!obj.sitters.containsKey(key)) obj.sitters.put(key, user.id)
        // now leave any previous seats we occupied
        seats foreach stand(user)
        // finally, if this game is now ready to go, start it
        if (seatKeys(table) forall obj.sitters.containsKey) startGame(table)
    }
  }

  override def leaveSeat (tableId :Int) {
    val user = User.require
    obj.tables.get(tableId) match {
      case null => _log.warning(s"No table for leaveSeat [user=$user, table=$tableId]")
      case table =>
        // we have to inspect each seat at this table to find the one we're sitting at
        for (key <- seatKeys(table)) {
          if (obj.sitters.get(key) == user.id) {
            obj.sitters.remove(key)
            checkDrop(table)
          }
        }
    }
  }

  override def sendChat (message :String) {
    val user = User.require
    obj.chat.emit(new ChatMessage(user.name, message))
  }

  protected def seatKeys (table :Table) =
    0 until table.seats map(s => new LobbyObject.Seat(table.id, s))

  protected def startGame (table :Table) {
    // TODO
    _log.info(s"TODO: start game ${table.id}")
  }

  protected def checkDrop (table :LobbyObject.Table) {
    if (!(seatKeys(table) exists obj.sitters.containsKey)) obj.tables.remove(table.id)
  }

  protected def occupiedSeats (user :User) =
    obj.sitters.asScala collect { case (k, id) if (id == user.id) => k }

  protected def stand (user :User)(seat :LobbyObject.Seat) = {
    obj.sitters.remove(seat)
    obj.tables.get(seat.tableId) match {
      case null => _log.warning(s"No table for left seat [seat=$seat]")
      case table => checkDrop(table)
    }
  }

  protected def forceStand (user :User) {
    // if this user is sitting at any table, remove them from it
    occupiedSeats(user) foreach stand(user)
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
