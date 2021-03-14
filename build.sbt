enablePlugins(JavaAppPackaging)

name := "assignment"
organization := "com.company"
version := "1.0"
scalaVersion := "2.13.5"

libraryDependencies ++= {
  val akkaHttpVersion = "10.2.4"
  val akkaVersion     = "2.6.13"
  val scalaTestV      = "3.2.3"
  val circeV          = "0.13.0"
  val akkaHttpCirceV  = "1.35.3"

  Seq(
    "com.typesafe.akka" %%  "akka-actor"  % akkaVersion,
    "com.typesafe.akka" %%  "akka-stream" % akkaVersion,
    "com.typesafe.akka" %%  "akka-http"   % akkaHttpVersion,

    "io.circe"          %% "circe-core"           % circeV,
    "io.circe"          %% "circe-generic"        % circeV,
    "io.circe"          %% "circe-generic-extras" % circeV,
    "io.circe"          %% "circe-parser"         % circeV,
    "de.heikoseeberger" %% "akka-http-circe"      % akkaHttpCirceV,

    "ch.qos.logback"    %   "logback-classic"   % "1.2.3",

    "com.typesafe.akka" %%  "akka-testkit"      % akkaVersion,
    "com.typesafe.akka" %%  "akka-http-testkit" % akkaHttpVersion % Test,
    "org.scalatest"     %%  "scalatest"         % scalaTestV      % Test
  )
}

Revolver.settings
