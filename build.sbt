import AssemblyKeys._

// ---------------------------------------------------------------------------------------------------------------------
//
// assembly setup
//
// ---------------------------------------------------------------------------------------------------------------------
assemblySettings

name := "sparqlclient"

version := "0.1.3"

scalaVersion := "2.10.4"

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

buildInfoPackage := "com.avalanche"

// ---------------------------------------------------------------------------------------------------------------------
//
// dependencies
//
// ---------------------------------------------------------------------------------------------------------------------

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case PathList("META-INF","maven","org.slf4j","slf4j-api", xs@_*) => MergeStrategy.first
  case PathList("org","slf4j",xs@_*) => MergeStrategy.first
  //  case "compiler.properties" => MergeStrategy.first
  //  case "reflect.properties" => MergeStrategy.first
  case x => old(x)
}
}

libraryDependencies ++= Seq()

libraryDependencies += ("org.scalatest" %% "scalatest" % "2.1.7" % "test")

libraryDependencies += ("net.databinder.dispatch" %% "dispatch-core" % "0.11.1")

//libraryDependencies += ("org.scala-saddle" %% "saddle-core" % "1.3.+")
