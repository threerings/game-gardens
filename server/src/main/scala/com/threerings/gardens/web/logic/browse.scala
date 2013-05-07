//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import java.util.{List, Collections, Comparator}

import com.samskivert.servlet.user.User
import com.samskivert.servlet.util.ParameterUtil
import com.samskivert.velocity.InvocationContext

import com.threerings.toybox.server.persist.GameRecord

import com.threerings.gardens.web.GardensApp

/** Displays a list of all registered games. */
class browse extends OptionalUserLogic {

  override def invoke (ctx :InvocationContext, app :GardensApp, user :User) {
    val category = ParameterUtil.getParameter(ctx.getRequest, "category", false)

    // load up the metadata for all of our games
    val games = if (category.equals("")) app.toyBoxRepo.loadGames()
    // load up the metadata for the games in this category
    else app.toyBoxRepo.loadGames(category)

    // sort our games by name
    Collections.sort(games, new Comparator[GameRecord]() {
      def compare (g1 :GameRecord, g2 :GameRecord) = g1.name.compareTo(g2.name)
    })
    ctx.put("games", games)
    ctx.put("category", category)
  }
}
