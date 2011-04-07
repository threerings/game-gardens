//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

import sbt._

class SReversi (info :ProjectInfo) extends DefaultProject(info)
{
  // need our local repository for locally installed snapshots
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  // we use scalatest for some testing
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"

  // pull in toybox and the other dependencies
  val narya = "com.threerings" % "narya" % "1.4-SNAPSHOT"
  val toybox = "com.threerings" % "toybox" % "1.1-SNAPSHOT"

  def runner (jvmOpts :List[String]) = forkRun(jvmOpts) match {
    case Some(fr :ForkScalaRun) => new ForkRun(fr)
    case _ => error("Alert! SBT exhibits teh suck.")
  }

  def genPolicyFile (name :String) :String = {
    val cpolicy = outputPath / name
    val out = new java.io.PrintWriter(cpolicy.asFile)
    val libdir = managedDependencyPath.asFile
    out.println("grant codeBase \"file://" + libdir.getAbsolutePath + "/-\" {")
    out.println("   permission java.security.AllPermission;")
    out.println("};")
    out.close
    cpolicy.asFile.getAbsolutePath
  }

  def clientAction (username :String) = task {
    val jvmOpts = List("-Dno_log_redir=true", "-Djava.security.manager=",
                       "-Djava.security.policy=" + genPolicyFile("client.policy"),
                       "-Dresource_url=file://" + (outputPath / "resources").asFile.getAbsolutePath)
    runner(jvmOpts).run("com.threerings.toybox.client.ToyBoxApp", runClasspath.get,
                        List("localhost", "47624", username, "secret"), log)
  } dependsOn(compile, copyResources)

  lazy val client = task { args =>
    if (args.length == 1) clientAction(args(0))
    else task { Some("Usage: client <username>") }
  }


  lazy val server = task {
    val out = new java.io.PrintWriter(outputPath / "classes" / "toybox.properties" asFile)
    out.println("resource_dir = " + outputPath.asFile.getAbsolutePath)
    out.println("resource_url = file://" + outputPath.asFile.getAbsolutePath)
    out.close

    val gjar = jarPath.asFile
    gjar.renameTo(new java.io.File(gjar.getParentFile, "reversi.jar"))

    val gconf :Path = "reversi.xml"
    val jvmOpts = List("-Dgame_conf=" + gconf.asFile.getAbsolutePath, "-Djava.security.manager=",
                       "-Djava.security.policy=" + genPolicyFile("server.policy"))
    runner(jvmOpts).run("com.threerings.toybox.server.ToyBoxServer", runClasspath.get,
                        List(), log)

    // <delete file="${deploy.dir}/classes/toybox.properties"/>
    // <delete file="${deploy.dir}/server.policy"/>
  } dependsOn(`package`)

}
