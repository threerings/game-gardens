//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server

// import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{HandlerList, ResourceHandler}
import org.eclipse.jetty.servlet.{ServletContextHandler, DefaultServlet, ServletHolder}

import com.threerings.gardens.web.GardensDispatcher

/** Customizes a Jetty server and handles HTTP requests. */
class GardensJetty (port :Int) extends Server(port) {

  def init () {
    // wire up our servlet context
    val ctx = new ServletContextHandler
    ctx.setContextPath("/")

    // wire up our servlets
    // ctx.addServlet(new ServletHolder(new HttpServlet() {
    //   override def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    //     try {
    //       val out = rsp.getWriter
    //       out.write("byebye\n")
    //       out.close
    //     } finally {
    //       exec.execute(new Runnable() {
    //         def run = shutdownSig.emit()
    //       })
    //     }
    //   }
    // }), "/shutdown")
    // ctx.addServlet(new ServletHolder(new QueryServlet), "/query/*")
    // ctx.addServlet(new ServletHolder(new ProjectsServlet), "/projects/*")
    // ctx.addServlet(new ServletHolder(new ProjectServlet), "/project/*")
    val gd = new ServletHolder(new GardensDispatcher)
    ctx.addServlet(gd, "*.wm")
    ctx.addServlet(gd, "*.jnlp")
    // ctx.addServlet(new ServletHolder(new DefaultServlet), "/*")

    val rsrc = new ResourceHandler
    rsrc.setDirectoriesListed(false)
    rsrc.setWelcomeFiles(Array("index.html"))
    rsrc.setResourceBase("server/src/main/web") // TODO: get from properties

    val handlers = new HandlerList
    handlers.setHandlers(Array(ctx, rsrc))
    setHandler(handlers)
  }
}
