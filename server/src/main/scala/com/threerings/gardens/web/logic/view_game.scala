//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import com.samskivert.servlet.user.User
import com.samskivert.servlet.util.{FriendlyException, ParameterUtil}
import com.samskivert.velocity.InvocationContext

import com.threerings.presents.server.InvocationException

import com.threerings.gardens.web.GardensApp

/**
 * Handles the logic behind creating and managing a game's metadata.
 */
class view_game extends OptionalUserLogic {

  override def invoke (ctx :InvocationContext, app :GardensApp, user :User) {
    val req = ctx.getRequest
    val gameId = ParameterUtil.requireIntParameter(req, "gameid", "error.invalid_gameid")
    val game = app.getToyBoxRepository.loadGame(gameId)
    if (game == null) {
      throw new FriendlyException("error.no_such_game")
    }
    ctx.put("game", game)
    val creator = app.getUserManager.getRepository.loadUser(game.maintainerId)
    if (creator != null) {
      ctx.put("creator", creator.username)
      ctx.put("creator_profile", PROFILE_URL + creator.username)
    }
    ctx.put("players", app.getToyBoxRepository.getOnlineCount(gameId))
    try {
      ctx.put("single_player", game.parseGameDefinition.isSinglePlayerPlayable)
    } catch {
      case ie :InvocationException =>
        val errmsg = if (ie.getCause == null) ie.getMessage else ie.getCause.getMessage
        _log.warning(s"Failed to parse gamedef [game=${game.which}, error=$errmsg].")
    }
  }

  // somewhat hacky link to creator profiles
  protected final val PROFILE_URL =
    "http://forums.gamegardens.com/discussion/mvnforum/viewmember?member="

  private val _log = java.util.logging.Logger.getLogger("gardens")
}
