//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import java.net.URL
import java.sql.Date
import javax.servlet.http.HttpServletRequest

import com.samskivert.servlet.RedirectException
import com.samskivert.servlet.user.User
import com.samskivert.servlet.util.FriendlyException
import com.samskivert.servlet.util.HTMLUtil
import com.samskivert.servlet.util.ParameterUtil

import com.samskivert.text.MessageUtil
import com.samskivert.velocity.InvocationContext

import com.google.inject.Inject

import com.threerings.presents.server.InvocationException

import com.threerings.toybox.data.GameDefinition
import com.threerings.toybox.server.ToyBoxConfig
import com.threerings.toybox.server.persist.GameRecord.Status
import com.threerings.toybox.server.persist.GameRecord

import com.threerings.gardens.web.GardensApp

/** Handles the logic behind creating and managing a game's metadata. */
class edit_game @Inject() (config :ToyBoxConfig) extends UserLogic {
  override def invoke (ctx :InvocationContext, app :GardensApp, user :User) {
    val req = ctx.getRequest

    // load up the game if an id was provided
    val gameId = ParameterUtil.getIntParameter(req, "gameid", 0, "error.invalid_gameid")
    val egame = if (gameId == 0) null else app.toyBoxRepo.loadGame(gameId)
    if (egame != null) {
      ctx.put("game", egame)
    }

    // make sure this user is the maintainer or an admin
    if (egame != null && !(user.userId == egame.maintainerId || user.isAdmin)) {
      throw new RedirectException(app.getProperty("access_denied_url"))
    }

    // determine where uploads should be sent
    val rurl = new URL(config.getResourceURL)
    val upurl = new URL(rurl.getProtocol, rurl.getHost, req.getContextPath() + "/upload_jar.wm")
    ctx.put("upload_url", upurl)

    // assume we're updating unless later overridden
    ctx.put("action", "update")

    // figure out what we're doing
    val action = ParameterUtil.getParameter(req, "action", false)
    if (action == "update") {
      if (egame == null) {
        throw new FriendlyException("error.no_such_game")
      }

      // read the parameters over top of the existing game
      populateGame(req, egame)

      // update it in the database
      app.toyBoxRepo.updateGame(egame)
      ctx.put("status", "edit_game.status.updated")

    } else if (action.equals("create")) {
      // we're still creating (just in case things fail)
      ctx.put("action", "create")

      // create a blank game and configure it
      val ngame = new GameRecord()
      ngame.setStatus(Status.PENDING)
      ngame.maintainerId = user.userId
      ngame.host = config.getServerHost
      ngame.digest = ""
      ngame.created = new Date(System.currentTimeMillis())
      ngame.lastUpdated = ngame.created
      ctx.put("game", ngame)

      // fill in the user supplied information
      populateGame(req, ngame)

      // insert it into the repository
      app.toyBoxRepo.insertGame(ngame)
      ctx.put("status", "edit_game.status.created")

      // now we can switch back to update mode
      ctx.put("action", "update")

    } else if (gameId != 0) {
      // nothing to do, the game is already in the context

    } else {
      ctx.put("action", "create")
      val ngame = new GameRecord()
      ngame.category = "other"
      ctx.put("game", ngame)
    }
  }

  protected def populateGame (req :HttpServletRequest, game :GameRecord) {
    // read in and validate our various bits
    game.name = requireString(req, "name", 50, true)
    game.category = requireString(req, "category", 255, false)
    game.definition = requireString(req, "definition", 2500, false)
    game.description = requireString(req, "description", 1000, true)
    game.instructions = requireString(req, "instructions", 5000, true)
    game.credits = requireString(req, "credits", 1000, true)

    // TODO: validate definition
    try {
      game.parseGameDefinition()
    } catch {
      case ie :InvocationException => throw new FriendlyException(ie.getCause match {
        case null => "edit_game.error.malformed_definition"
        case cause => MessageUtil.tcompose("edit_game.error.malformed_definition_why",
                                           cause.getMessage)
      })
    }

    // TODO: set the status to PUBLISHED if all is groovy?
  }

  protected def requireString (req :HttpServletRequest, name :String,
                               maxLength :Int, entify :Boolean) = {
    val value = ParameterUtil.requireParameter(
      req, name, MessageUtil.compose("error.missing_field", "f." + name))
    if (value.length > maxLength) {
      throw new FriendlyException(MessageUtil.compose("error.field_too_long", "f." + name,
                                                      MessageUtil.taint("" + maxLength)))
    }
    if (entify) HTMLUtil.restrictHTML(value, true, true, true) else value
  }
}
