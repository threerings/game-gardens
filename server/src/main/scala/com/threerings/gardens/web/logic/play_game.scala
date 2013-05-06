//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import com.samskivert.velocity.InvocationContext
import com.samskivert.servlet.user.User

import com.threerings.toybox.server.ToyBoxConfig

import com.threerings.gardens.web.GardensApp

/** Fires up a game in an applet. */
class play_game extends view_game {

  override def invoke (ctx :InvocationContext, app :GardensApp, user :User) {
    super.invoke(ctx, app, user)
    ctx.put("port", ToyBoxConfig.getServerPort)
    ctx.put("resource_url", ToyBoxConfig.getResourceURL)
  }
}
