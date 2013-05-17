//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web.logic

import com.samskivert.servlet.RedirectException
import com.samskivert.servlet.user.{Password, User}
import com.samskivert.servlet.util.{FriendlyException, ParameterUtil}
import com.samskivert.velocity.InvocationContext

import com.threerings.gardens.web.GardensApp

class login extends AuthLogic {

  override def invoke (ctx :InvocationContext, app :GardensApp, user :User) {
    val (req, rsp) = (ctx.getRequest, ctx.getResponse)
    ParameterUtil.getParameter(req, "action", true) match {
      case "login" =>
        val username = ParameterUtil.requireParameter(req, "username", "error.missing_username")
        val password = ParameterUtil.requireParameter(req, "password", "error.missing_password")
        val user = app.userManager.getRepository.loadUser(username)
        if (user == null) throw new FriendlyException("login.no_such_user")
        if (!user.passwordsMatch(Password.makeFromClear(password)))
          throw new FriendlyException("login.invalid_password")
        effectLogin(ctx, app, user)
        throw new RedirectException("/")

      case "logout" =>
        app.userManager.logout(req, rsp)
        ctx.put("status", "login.logged_out")

      case _ => // just display the login page
    }
  }
}
