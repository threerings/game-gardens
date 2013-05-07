//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server

import java.util.Properties

import com.samskivert.util.Config
import com.threerings.toybox.server.ToyBoxConfig

class GardensConfig (config :Config) extends ToyBoxConfig(config) {

  /** Indicates that we're in testing mode (i.e. not in production). */
  def testMode :Boolean = _config.getValue("test_mode", false)

  /** Returns the port on which our HTTP server listens. */
  def httpServerPort :Int = _config.getValue("http_server_port", 8080)

  /** Returns our web configuration. */
  def webConfig :Properties = _config.getSubProperties("web")
}
