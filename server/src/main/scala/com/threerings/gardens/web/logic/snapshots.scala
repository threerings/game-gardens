//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import java.util.List
import java.util.HashMap

import com.samskivert.servlet.util.ParameterUtil
import com.samskivert.velocity.Application
import com.samskivert.velocity.InvocationContext
import com.samskivert.velocity.Logic

import com.threerings.toybox.server.persist.GameRecord

import com.threerings.gardens.web.GardensApp

/** Provides information on recently added, updated and most-popular games. */
class snapshots extends Logic {
  override def invoke (app :Application, ctx :InvocationContext) {
    val gtapp = app.asInstanceOf[GardensApp]
    val category = ParameterUtil.getParameter(ctx.getRequest, "type", ADDED)

    val games = if (POPULAR == category) getGames(
      POPULAR, gtapp.getToyBoxRepository.loadPopularGames(SNAPSHOT_COUNT))
    else if (UPDATED == category) getGames(
      UPDATED, gtapp.getToyBoxRepository.loadRecentlyUpdated(SNAPSHOT_COUNT))
    else getGames(ADDED, gtapp.getToyBoxRepository.loadRecentlyAdded(SNAPSHOT_COUNT))

    ctx.put("games", games)
  }

  protected  def getGames (category :String, func : => List[GameRecord]) = {
    val games = _cache.get(category)
    val now = System.currentTimeMillis
    if (games == null || (now - games.loadStamp) > REFRESH_INTERVAL) {
      _cache.put(category, CachedGames(now, func))
    }
    games.games
  }

  protected trait RefreshFunc {
    def refresh () :List[GameRecord]
  }

  protected case class CachedGames (val loadStamp :Long, val games :List[GameRecord])

  protected val _cache = new HashMap[String,CachedGames]

  protected final val ADDED = "added"
  protected final val UPDATED = "updated"
  protected final val POPULAR = "popular"

  protected final val SNAPSHOT_COUNT = 9
  protected final val REFRESH_INTERVAL = 15 * 60 * 1000L
}
