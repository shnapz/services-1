import sbt._
import Keys._
import pulse.plugin._
import pulse.plugin.dependencies._

libraryDependencies ++= Seq (
  local.impulse.common,
  local.impulse.config,
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
  _test(local.finch.test),
  _test(scalatest.core)
)

settings.common

publishing.settings

local.settings

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "BUILD" => MergeStrategy.discard
  case _ => MergeStrategy.deduplicate
}