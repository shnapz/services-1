import sbt._
import Keys._
import pulse.plugin._
import pulse.plugin.dependencies._
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.docker.Dockerfile._

libraryDependencies ++= Seq (
  cats.all,
  fs2.core,
  refined.core,
  log4s.core,
  sl4j.simple,
  local.finch.core,
  local.finch.circe,
  local.finagle.core,
  local.finagle.server,
  local.circe.core,
  local.circe.generic,
  local.scopt.core,
  local.avro.core,
  typesafe.config,
  _test(local.finch.test),
  _test(scalatest.core)
)

settings.common

publishing.settings

local.settings

local.docker

dockerRepository := Some("pulse")
dockerCommands := Seq(
  Cmd("FROM", "openjdk:8"),
  Cmd("COPY", "opt/docker/lib/*.jar", "/example-assembly-0.1-SNAPSHOT.jar"),
  ExecCmd("CMD", "java", "-jar",
    "-Djava.rmi.server.hostname=example-app",
    "-Dcom.sun.management.jmxremote",
    "-Dcom.sun.management.jmxremote.port=4000",
    "-Dcom.sun.management.jmxremote.rmi.port=4000",
    "-Dcom.sun.management.jmxremote.local.only=false",
    "-Dcom.sun.management.jmxremote.authenticate=false",
    "-Dcom.sun.management.jmxremote.ssl=false",
    "/example-assembly-0.1-SNAPSHOT.jar")
)

