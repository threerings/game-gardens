//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web

import java.util.Properties
import java.util.logging.{Logger, Level}
import javax.servlet.{ServletConfig, ServletContext}

import com.google.inject.Inject
import com.google.inject.name.Named

import com.samskivert.depot.PersistenceContext
import com.samskivert.servlet.SiteIdentifiers
import com.samskivert.servlet.user.Password
import com.samskivert.servlet.user.Username
import com.samskivert.util.PropertiesUtil
import com.samskivert.velocity.Application

import com.threerings.user.OOOUser
import com.threerings.user.depot.DepotUserManager

import com.threerings.gardens.server.GardensConfig
import com.threerings.toybox.server.persist.ToyBoxRepository

/** Contains references to application-wide resources (like the database repository) and handles
  * initialization and cleanup for those resources.
  */
class GardensApp @Inject() (
  config :GardensConfig,
  @Named("userdb") pctx :PersistenceContext,
  tbrepo :ToyBoxRepository
) extends Application {

  /** Returns the user manager in use by this application. */
  val userManager = new DepotUserManager(config.webConfig, pctx)

  /** Provides access to the toybox repository. */
  def toyBoxRepo = tbrepo

  /** Looks up a property in our {@code gardens.properties} application config file. */
  def getProperty (key :String) = config.webConfig.getProperty(key)

  /** Shut down the user management application. */
  override def shutdown () {
    try {
      userManager.shutdown()
      _log.info("Game Gardens application shutdown.")
    } catch {
      case t :Throwable => _log.log(Level.WARNING, "Error shutting down repository", t)
    }
  }

  override protected def getInitParameter (svlcfg :ServletConfig, key :String) = key match {
    case "messages_path" => "messages"
    case "org.apache.velocity.properties" => "/velocity.properties"
    case _ => super.getInitParameter(svlcfg, key)
  }

  override protected def createSiteIdentifier (ctx :ServletContext) = SiteIdentifiers.single(
    OOOUser.GAMEGARDENS_SITE_ID, "gardens")

  override protected def didInit (svlcfg :ServletConfig) {
    super.didInit(svlcfg)
    // if we're in test mode, create a fake 'tester' account
    if (config.testMode) {
      userManager.getRepository.createUser(
        new Username("tester"), Password.makeFromClear("tester"),
        "tester@test.com", OOOUser.GAMEGARDENS_SITE_ID, 0)
      _log.info("Created test account 'tester'.")
    }
  }

  protected val _log = Logger.getLogger("gardens")
}
