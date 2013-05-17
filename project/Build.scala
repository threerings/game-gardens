import sbt._
import sbt.Keys._

object GardensBuild extends Build {
  val builder = new samskivert.ProjectBuilder("pom.xml") {
    override val globalSettings = Seq(
      crossPaths    := false,
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
      javacOptions  ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
      javaOptions   ++= Seq("-ea"),
      fork in Compile := true,
      autoScalaLibrary in Compile := false, // no scala-library dependency (except for tests)
      libraryDependencies ++= Seq(
        "com.novocode" % "junit-interface" % "0.8" % "test->default" // make junit work
      )
    )
    override def projectSettings (name :String, pom :pomutil.POM) = name match {
      case "toybox" => Seq(
        // a bunch of unfixable (without abandoning 1.6) AWT generics warnings
        javacOptions ++= Seq("-Xlint:-rawtypes", "-Xlint:-unchecked")
      )
      case "server" => Seq(
        resolvers += "ooo-maven" at "http://ooo-maven.googlecode.com/hg/repository",
        autoScalaLibrary := true // we want scala-library back
      )
      case _ => Nil
    }
  }

  lazy val api = builder("api")
  lazy val core = builder("core")
  lazy val client = builder("client")
  lazy val toybox = builder("toybox")
  lazy val server = builder("server")

  // one giant fruit roll-up to bring them all together
  lazy val gardens = builder.root.aggregate(api, core, toybox, client, server)
}
