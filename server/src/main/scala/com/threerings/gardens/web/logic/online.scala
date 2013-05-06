//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import java.util.List

import com.samskivert.util.Tuple
import com.samskivert.velocity.Application
import com.samskivert.velocity.InvocationContext
import com.samskivert.velocity.Logic

import com.threerings.toybox.server.persist.OnlineRecord

import com.threerings.gardens.web.GardensApp

/** Provides a listing of all games that have players online right now. */
class online extends Logic {
  override def invoke (app :Application, ctx :InvocationContext) {
    val gtapp = app.asInstanceOf[GardensApp]
    val now = System.currentTimeMillis
    if (_online == null || now - _lastUpdated > UPDATE_INTERVAL) {
      _online = gtapp.getToyBoxRepository.getOnlineCounts
      _lastUpdated = now
    }
    ctx.put("online", _online)
  }

  protected var  _online :List[Tuple[String,OnlineRecord]] = _
  protected var _lastUpdated = 0L

  protected final val UPDATE_INTERVAL = 60 * 1000L
}
