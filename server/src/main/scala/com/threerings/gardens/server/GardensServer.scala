//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server

import com.google.inject.Injector

import com.samskivert.jdbc.StaticConnectionProvider
import com.samskivert.util.Config

import com.threerings.presents.server.PresentsServer
import com.threerings.toybox.server.{ToyBoxConfig, ToyBoxServer}

/** Main entry point for Game Gardens server. */
object GardensServer {

  /** Configures dependencies needed by the Gardens services. */
  class Module extends ToyBoxServer.ToyBoxModule {
    override protected def configure () {
      super.configure()
      bind(classOf[GardensConfig]).toInstance(config);
    }
    override protected lazy val config = {
      val props = ToyBoxConfig.testConfig
      props.setProperty("http_server_port", "8080") // TODO: get these proper-like
      props.setProperty("web.login_url", "register.wm?from=%R")
      props.setProperty("web.access_denied_url", "access_denied.wm")
      props.setProperty("web.auth_cookie.strip_hostname", "true")
      new GardensConfig(new Config(props))
    }
    override protected def conprov = StaticConnectionProvider.forTest("gardens")
    override protected def autherClass = classOf[GardensAuther]
  }

  class Server extends ToyBoxServer {
    override def init (injector :Injector) {
      super.init(injector)

      // Properties props = new Properties();
      // props.setProperty("nexus.node", "test");
      // props.setProperty("nexus.hostname", "localhost");
      // props.setProperty("nexus.rpc_timeout", "1000");
      // NexusConfig config = new NexusConfig(props);

      // // create our server
      // ExecutorService exec = Executors.newFixedThreadPool(3);
      // NexusServer server = new NexusServer(config, exec);

      // // set up a connection manager and listen on a port
      // final JVMConnectionManager jvmmgr = new JVMConnectionManager(server.getSessionManager());
      // jvmmgr.listen(config.publicHostname, 1234);
      // jvmmgr.start();

      // // set up a Jetty instance and our GWTIO servlet
      // final GWTConnectionManager gwtmgr = new GWTConnectionManager(
      //     server.getSessionManager(), new ChatSerializer(), config.publicHostname, 6502);
      // gwtmgr.setDocRoot(new File("target/chat-demo-1.0-SNAPSHOT"));
      // gwtmgr.start();

      _jetty = injector.getInstance(classOf[GardensJetty])
      _jetty.init()
      _jetty.start()
    }

    override def invokerDidShutdown () {
      _jetty.stop()
      _jetty.join()
    }

    protected var _jetty :GardensJetty = _
  }

  def main (args :Array[String]) {
    PresentsServer.runServer(new Module(), new PresentsServer.PresentsServerModule(classOf[Server]))
  }
}
