import pulse.plugin._

organization in Global := "impulse-io"

scalaVersion in Global := "2.11.8"

lazy val pulse_services = project.in(file(".")).aggregate(core, example)

lazy val core	   = project

lazy val example = project.dependsOn(core).enablePlugins(DockerPlugin, JavaAppPackaging)

settings.common

publishing.ignore

