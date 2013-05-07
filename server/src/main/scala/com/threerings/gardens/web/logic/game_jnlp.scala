//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import java.util.Calendar
import java.util.logging.Level
import java.util.regex.Matcher
import java.util.regex.Pattern

import java.net.URL
import javax.servlet.http.HttpServletRequest

import com.google.inject.Inject

import com.samskivert.servlet.util.FriendlyException

import com.samskivert.velocity.Application
import com.samskivert.velocity.InvocationContext
import com.samskivert.velocity.Logic

import com.threerings.toybox.server.ToyBoxConfig
import com.threerings.toybox.server.persist.GameRecord

import com.threerings.gardens.web.GardensApp

/** Provides a JNLP file for a particular game. */
class game_jnlp @Inject() (config :ToyBoxConfig) extends Logic {

  override def invoke (app :Application, ctx :InvocationContext) {
    val gtapp = app.asInstanceOf[GardensApp]
    val req = ctx.getRequest

    // we get our game id from the path
    val rpath = req.getServletPath()
    val m = _jnlppat.matcher(rpath)
    val gameId = if (m.matches()) {
      try Integer.parseInt(m.group(1))
      catch {
        case e :Exception => -1
      }
    } else -1
    if (gameId < 0) {
      throw new FriendlyException("error.invalid_gameid")
    }

    // load up the game
    val game = gtapp.toyBoxRepo.loadGame(gameId)
    if (game == null) {
      throw new FriendlyException("error.no_such_game")
    }
    ctx.put("game", game)

    // fake up a last modified header
    val cal = Calendar.getInstance
    val (year, day) = (cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
    cal.setTime(game.lastUpdated)
    // if it was last updated today, use the current time as the last modification as we don't have
    // finer granularity
    if (year == cal.get(Calendar.YEAR) && day == cal.get(Calendar.DAY_OF_YEAR)) {
      cal.setTimeInMillis(System.currentTimeMillis)
    } else {
      // otherwise claim last modification at 11:59:59 on the known date
      cal.set(Calendar.HOUR_OF_DAY, 23)
      cal.set(Calendar.MINUTE, 59)
      cal.set(Calendar.SECOND, 59)
    }
    val lastModified = cal.getTime.getTime
    ctx.getResponse.setDateHeader("Last-Modified", lastModified)

    val path = CLIENT_PATH
    val codebase = try new URL("http", game.host, path)
    catch {
      case e :Exception =>
        _log.log(Level.WARNING, s"Error creating codebase URL [ghost=${game.host}, path=$path].", e)
        throw new FriendlyException("error.internal_error")
    }

    ctx.put("base_path", req.getContextPath)
    ctx.put("codebase", codebase.toString)
    ctx.put("server", game.host)
    ctx.put("port", config.getServerPort)
    ctx.put("resource_url", config.getResourceURL)

    ctx.getResponse.setContentType("application/x-java-jnlp-file")
  }

  protected val _jnlppat = Pattern.compile("/game_([0-9]+).jnlp")
  protected final val CLIENT_PATH = "/client"
  private val _log = java.util.logging.Logger.getLogger("game_jnlp")
}
