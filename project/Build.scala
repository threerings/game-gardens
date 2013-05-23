import sbt._
import sbt.Keys._

object GardensBuild extends samskivert.MavenBuild {

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

  override def moduleSettings (name :String, pom :pomutil.POM) = name match {
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

  override def profiles = Seq("server")
}
