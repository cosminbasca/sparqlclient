import AssemblyKeys._

// ---------------------------------------------------------------------------------------------------------------------
//
// assembly setup
//
// ---------------------------------------------------------------------------------------------------------------------
assemblySettings

name := "sparqlclient"

organization := "com.sparqlclient"

version := "0.2.4"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions ++= Seq("-optimize", "-Yinline-warnings", "-feature", "-deprecation")


excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {
    _.data.getName == "minlog-1.2.jar"
  }
}

// ---------------------------------------------------------------------------------------------------------------------
//
// build info setup
//
// ---------------------------------------------------------------------------------------------------------------------
buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "com.sparqlclient"

// ---------------------------------------------------------------------------------------------------------------------
//
// dependencies
//
// ---------------------------------------------------------------------------------------------------------------------

libraryDependencies ++= Seq()

libraryDependencies += ("org.scalatest" %% "scalatest" % "2.1.7" % "test")

libraryDependencies += ("net.databinder.dispatch" %% "dispatch-core" % "0.11.1")

libraryDependencies += ("net.liftweb" %% "lift-json" % "2.6-RC1")