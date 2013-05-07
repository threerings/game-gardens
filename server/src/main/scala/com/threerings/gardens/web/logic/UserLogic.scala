//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import com.samskivert.velocity.Application
import com.samskivert.velocity.InvocationContext
import com.samskivert.velocity.Logic

import com.samskivert.servlet.user.User

import com.threerings.gardens.web.GardensApp

/** A base logic class for pages that require an authenticated user. */
abstract class UserLogic extends Logic {

  /** Logic classes should implement this method to perform their normal duties.
    *
    * @param ctx the context in which the request is being invoked.
    * @param app the web application.
    * @param user the user record for the authenticated user.
    */
  def invoke (ctx :InvocationContext, app :GardensApp, user :User)

  // documentation inherited from interface
  override def invoke (app :Application, ctx :InvocationContext) {
    val gtapp = app.asInstanceOf[GardensApp]
    val user = gtapp.userManager.requireUser(ctx.getRequest)
    ctx.put("user", user)
    invoke(ctx, gtapp, user)
  }
}
