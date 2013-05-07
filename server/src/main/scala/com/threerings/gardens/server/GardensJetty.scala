//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server

import com.google.inject.{Inject, Injector, Singleton}

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{HandlerList, ResourceHandler}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}

import com.threerings.gardens.web.GardensDispatcher

/** Customizes a Jetty server and handles HTTP requests. */
@Singleton class GardensJetty @Inject() (config :GardensConfig, injector :Injector)
    extends Server(config.httpServerPort) {

  def init () {
    // wire up our servlet context
    val ctx = new ServletContextHandler
    ctx.setContextPath("/")

    // wire up our servlets
    val gd = new ServletHolder(injector.getInstance(classOf[GardensDispatcher]))
    ctx.addServlet(gd, "*.wm")
    ctx.addServlet(gd, "*.jnlp")

    val rsrc = new ResourceHandler
    rsrc.setDirectoriesListed(false)
    rsrc.setWelcomeFiles(Array("index.html"))
    rsrc.setResourceBase("server/src/main/web") // TODO: get from properties

    val handlers = new HandlerList
    handlers.setHandlers(Array(ctx, rsrc))
    setHandler(handlers)
  }
}
