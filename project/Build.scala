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
    ),
    resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI + "/.m2/repository"
  )

  override def moduleSettings (name :String, pom :pomutil.POM) = name match {
    case "toybox" => seq(
      // a bunch of unfixable (without abandoning 1.6) AWT generics warnings
      javacOptions ++= Seq("-Xlint:-rawtypes", "-Xlint:-unchecked")
    )
    case "server" => spray.revolver.RevolverPlugin.Revolver.settings ++ seq(
      resolvers += "ooo-maven" at "http://ooo-maven.googlecode.com/hg/repository",
      autoScalaLibrary := true, // we want scala-library back
      // we run the test server with its normal compile depends, so add hsqldb there;
      // in maven its run as a "test" and the POM thus lists hsqldb as a test depend
      libraryDependencies ++= Seq(
        "org.hsqldb" % "hsqldb" % "2.2.4"
      )
    )
    case _ => Nil
  }

  override def profiles = Seq("server")
}
