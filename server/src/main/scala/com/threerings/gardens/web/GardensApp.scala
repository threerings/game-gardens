//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web

import java.io.File
import java.util.Properties
import java.util.logging.{Logger, Level}
import javax.servlet.{ServletConfig, ServletContext}

import com.samskivert.depot.PersistenceContext
import com.samskivert.io.PersistenceException
import com.samskivert.jdbc.{ConnectionProvider, StaticConnectionProvider}
import com.samskivert.servlet.SiteIdentifiers
import com.samskivert.servlet.user.UserManager
import com.samskivert.util.{ConfigUtil, PropertiesUtil, ServiceUnavailableException}
import com.samskivert.velocity.Application

import com.threerings.user.OOOUser

import com.threerings.toybox.server.ToyBoxConfig
import com.threerings.toybox.server.persist.ToyBoxRepository

/** Contains references to application-wide resources (like the database repository) and handles
  * initialization and cleanup for those resources.
  */
class GardensApp extends Application {

  /** Returns the connection provider in use by this application. */
  def getConnectionProvider = _conprov

  /** Returns the user manager in use by this application. */
  def getUserManager = _usermgr

  /** Provides access to the toybox repository. */
  def getToyBoxRepository = _tbrepo

  /** Looks up a property in our {@code gardens.properties} application config file. */
  def getProperty (key :String) = _config.getProperty(key)

  /** Shut down the user management application. */
  override def shutdown () {
    try {
      _usermgr.shutdown()
      _log.info("Game Gardens application shutdown.")
    } catch {
      case t :Throwable => _log.log(Level.WARNING, "Error shutting down repository", t)
    }
  }

  override protected def getInitParameter (config :ServletConfig, key :String) = key match {
    case "messages_path" => "messages"
    case "org.apache.velocity.properties" => "/velocity.properties"
    case _ => super.getInitParameter(config, key)
  }

  /** Initialize the user management application. */
  override protected def willInit (config :ServletConfig) {
    super.willInit(config)

    try {
      // load up our configuration properties
      _config = ToyBoxConfig.config.getSubProperties("web")

      // create a static connection provider
      _conprov = new StaticConnectionProvider(ToyBoxConfig.getJDBCConfig)

      // create our repositories and managers
      val umclass = _config.getProperty("webapp_auth", classOf[UserManager].getName)
      _usermgr = Class.forName(umclass).newInstance.asInstanceOf[UserManager]
      _usermgr.init(_config, _conprov)

      val pctx = new PersistenceContext()
      pctx.init(ToyBoxRepository.GAME_DB_IDENT, _conprov, null)
      _tbrepo = new ToyBoxRepository(pctx)
      pctx.initializeRepositories(true)

      // load up our build stamp so that we can report it
      val bstamp = PropertiesUtil.loadAndGet("build.properties", "build.time")
      _log.info(s"Game Gardens application initialized [built=$bstamp].")

    } catch {
      case t :Throwable => _log.log(Level.WARNING, "Error initializing application", t)
    }
  }

  override protected def createSiteIdentifier (ctx :ServletContext) = SiteIdentifiers.single(
    OOOUser.GAMEGARDENS_SITE_ID, "gardens")

  // these are initialized in willInit
  protected var _usermgr :UserManager = _
  protected var _conprov :ConnectionProvider = _
  protected var _tbrepo :ToyBoxRepository = _
  protected var _config :Properties = _

  protected val _log = Logger.getLogger("gardens")
}
