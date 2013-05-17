//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server

import java.util.Properties
import java.util.concurrent.Executors

import com.google.inject.Injector

import com.samskivert.jdbc.{ConnectionProvider, StaticConnectionProvider}
import com.samskivert.util.{Config, Lifecycle}

import com.threerings.nexus.server.{JVMConnectionManager, NexusConfig, NexusServer}
import com.threerings.presents.server.PresentsServer
import com.threerings.toybox.server.{ToyBoxConfig, ToyBoxServer}

import com.threerings.gardens.lobby.LobbyManager
import com.threerings.gardens.user.UserManager

/** Main entry point for Game Gardens server. */
object GardensServer {

  /** Configures dependencies needed by the Gardens services. */
  class Module (props :Properties) extends ToyBoxServer.ToyBoxModule {
    override protected def configure () {
      super.configure()
      bind(classOf[GardensConfig]).toInstance(config);
    }
    override protected lazy val config = new GardensConfig(new Config(props))
    override protected def conprov = StaticConnectionProvider.forTest("gardens")
    override protected def autherClass = classOf[GardensAuther]
  }

  class Server extends ToyBoxServer {
    override def init (injector :Injector) {
      super.init(injector)

      // TODO: get these from properties file when in production
      val props = new Properties()
      props.setProperty("nexus.node", "test")
      props.setProperty("nexus.hostname", "localhost")
      props.setProperty("nexus.rpc_timeout", "1000")
      val config = new NexusConfig(props)

      // create our server
      val exec = Executors.newFixedThreadPool(3)
      val server = new NexusServer(config, exec)

      // set up a connection manager and listen on a port
      val jvmmgr = new JVMConnectionManager(server.getSessionManager())
      jvmmgr.listen(config.publicHostname, 1234)
      jvmmgr.start()

      // create our user and lobby managers (they register themselves with Nexus)
      val lobbyMgr = new LobbyManager(server)
      new UserManager(server, injector.getInstance(classOf[ConnectionProvider]), lobbyMgr)

      // shut things down when PresentsServer shuts us down
      val cycle = injector.getInstance(classOf[Lifecycle])
      cycle.addComponent(new Lifecycle.ShutdownComponent() {
        def shutdown () {
          exec.shutdown()
          jvmmgr.disconnect()
          jvmmgr.shutdown()
        }
      })

      _jetty = injector.getInstance(classOf[GardensJetty])
      _jetty.init(server, null) // TODO: serializer
      _jetty.start()
    }

    override def invokerDidShutdown () {
      _jetty.stop()
      _jetty.join()
    }

    protected var _jetty :GardensJetty = _
  }

  def main (args :Array[String]) {
    // TODO: load these from a properties file when deployed in production
    val props = ToyBoxConfig.testConfig
    props.setProperty("http_server_port", "8080")
    props.setProperty("test_mode", "true")
    props.setProperty("web.login_url", "test_auth.wm?from=%R")
    props.setProperty("web.access_denied_url", "access_denied.wm")

    PresentsServer.runServer(new Module(props), new
      PresentsServer.PresentsServerModule(classOf[Server]))
  }
}
