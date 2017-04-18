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
    val finch = "0.13.1"
    val circe = "0.7.0"
    val scopt = "3.5.0"
    val avro = "1.8.1"
    val impulse = "1.0.23"
    object twitter {
      val server  = "1.27.0"
      val finagle = "6.42.0"
    }
  }

  object avro {
    val core = "org.apache.avro" % "avro" % versions.avro
  }

  object scopt {
    val core = "com.github.scopt" %% "scopt" % versions.scopt
  }

  object finch {
    val core  = "com.github.finagle" %% "finch-core"  % versions.finch
    val circe = "com.github.finagle" %% "finch-circe" % versions.finch
    val test  = "com.github.finagle" %% "finch-test"  % versions.finch
  }

  object circe {
    val core    = "io.circe" %% "circe-core"    % versions.circe
    val generic = "io.circe" %% "circe-generic" % versions.circe
    val parser  = "io.circe" %% "circe-parser"  % versions.circe
  }

  object finagle {
    val core   = "com.twitter" %% "finagle-http"   % versions.twitter.finagle
    val server = "com.twitter" %% "twitter-server" % versions.twitter.server
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
