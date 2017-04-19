import sbt._, Keys._
import pulse.plugin._
import sbt.Keys._
import sbtassembly.AssemblyKeys._
import sbtassembly._
import bintray.BintrayKeys._
import sbt._, Keys._
import sbtassembly._, AssemblyKeys._, AssemblyPlugin._
import com.typesafe.sbt.SbtNativePackager._

object local {

  object versions {
    val impulse = "1.0.23"
  }

  object impulse {
    val common = "impulse-io" %% "common" % versions.impulse
    val config = "impulse-io" %% "config" % versions.impulse
  }

  lazy val sbtAssemblySettings = Seq(
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case "BUILD" => MergeStrategy.discard
      case _ => MergeStrategy.deduplicate
    }
  )

  lazy val sbtDockerSettings = Seq(
    // Remove all jar mappings in universal and append the fat jar
    mappings in Universal := {
      val universalMappings = (mappings in Universal).value
      val fatJar = (assembly in Compile).value
      val filtered = universalMappings.filter {
        case (file, name) => !name.endsWith(".jar")
      }
      filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    }
  )

  def docker = sbtAssemblySettings ++ sbtDockerSettings

  def settings = Seq(
    bintrayOrganization := Some("impulse-io"),
    publishMavenStyle   := true,
    scalacOptions ++= Seq("-feature")
  )


}
