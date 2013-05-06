//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web

import java.io.File
import java.util.Properties
import java.util.regex.Pattern
import javax.servlet.ServletConfig

import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.RuntimeSingleton

import com.samskivert.velocity.{ClasspathResourceLoader, DispatcherServlet, InvocationContext}

/** Handles some custom business with regard to Velocity dispatching. */
class GardensDispatcher extends DispatcherServlet {

  override def selectTemplate (siteId :Int, ctx :InvocationContext) = {
    // do some massaging of the path to handle JNLP files in a way that doesn't confuse Java Web
    // Start (it doesn't allow JNLP files to differ only by query parameters)
    RuntimeSingleton.getRuntimeServices.getTemplate(ctx.getRequest.getServletPath match {
      case path if (_jnlppat.matcher(path).matches) => "/game_jnlp.wm"
      case path => path
    })
  }

  override protected def resolveLogic (path :String) = super.resolveLogic(
    if (_jnlppat.matcher(path).matches()) "/game_jnlp.wm" else path)

  override protected def createApp (config :ServletConfig) = new GardensApp

  override protected def getLogicPackage (config :ServletConfig) = "com.threerings.gardens.web.logic"

  override protected def loadVelocityProps (config :ServletConfig) = {
    val props = Map("directive.foreach.counter.name" -> "vidx",
                    "directive.foreach.counter.initial.value" -> "0",
                    // We don't use velocimacros and don't care to hear Velocity complain about
                    // not being able to load the global velocimacro library.
                    "velocimacro.library" -> "",
                    // This configures Velocity such that macros defined inline in a template are
                    // only visible to that template. I have no idea why this is not the default.
                    "velocimacro.permissions.allow.inline.local.scope" -> "true")
    (new Properties /: props) { (vp, e) => vp.setProperty(e._1, e._2); vp }
  }

  override protected def configureResourceManager (config :ServletConfig, props :Properties) {
    val devdir = new File("server/src/main/resources/web")
    if (devdir.exists) {
      props.setProperty("file.resource.loader.path", devdir.getPath)
      _log.info("Velocity loading directly from " + devdir + ".")
    } else {
      props.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
      props.setProperty("classpath.resource.loader.class", classOf[GardensResourceLoader].getName());
    }
  }

  protected val _jnlppat = Pattern.compile("/game_[0-9]+.jnlp")
  protected val _log = java.util.logging.Logger.getLogger("gardens")
}
