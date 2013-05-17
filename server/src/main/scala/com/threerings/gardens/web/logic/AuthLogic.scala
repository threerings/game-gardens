//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import javax.servlet.http.{Cookie, HttpServletResponse}

import com.samskivert.servlet.util.CookieUtil
import com.samskivert.velocity.InvocationContext
import com.threerings.user.OOOUser

import com.threerings.gardens.web.GardensApp

abstract class AuthLogic extends OptionalUserLogic {

  protected def effectLogin (ctx :InvocationContext, app :GardensApp, user :OOOUser) {
    val auth = app.userManager.effectLogin(user, ExpireDays, ctx.getRequest, ctx.getResponse)
    val nmcook = new Cookie("nm_", user.username)
    CookieUtil.widenDomain(ctx.getRequest, nmcook)
    nmcook.setPath("/")
    nmcook.setMaxAge(ExpireDays*24*60*60)
    ctx.getResponse.addCookie(nmcook)
    _log.info(s"Logged user in [id=${user.userId}, name=${user.username}, auth=$auth]")
  }

  protected final val ExpireDays = 14

  protected val _log = java.util.logging.Logger.getLogger("auth")
}
