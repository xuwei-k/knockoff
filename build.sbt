import sbtrelease.ReleaseStateTransformations._
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val tagName = Def.setting{
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

val Scala212 = "2.12.10"

val tagOrHash = Def.setting {
  if(isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}

val unusedWarnings = Def.setting(
  Seq("-Ywarn-unused:imports")
)

val parserCombinatorsVersion = settingKey[String]("")

val commonSettings = Def.settings(
  releaseTagName := tagName.value,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(
      action = { state =>
        val extracted = Project extract state
        extracted.runAggregated(PgpKeys.publishSigned in Global in extracted.get(thisProjectRef), state)
      },
      enableCrossBuild = true
    ),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  ),
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala212, "2.13.1"),
  organization := "org.foundweekends",
  scalacOptions in (Compile, doc) ++= {
    val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
    Seq(
      "-sourcepath",
      base,
      "-doc-source-url",
      "https://github.com/foundweekends/knockoff/tree/" + tagOrHash.value + "€{FILE_PATH}.scala"
    )
  },
  scalacOptions ++= unusedWarnings.value,
  scalacOptions ++= Seq("-language:_", "-deprecation", "-Xlint"),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Seq("-Xfuture")
      case _ =>
        Nil
    }
  },
  Seq(Compile, Test).flatMap(c =>
    scalacOptions in (c, console) --= unusedWarnings.value
  ),
)

commonSettings

val knockoff = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    commonSettings,
    buildInfoPackage := "knockoff",
    buildInfoObject := "KnockoffBuildInfo",
    name := "knockoff",
    libraryDependencies += {
      "org.scalatest" %% "scalatest" % "3.1.1" % "test"
    },
    libraryDependencies ++= Seq(
      "net.sf.jtidy" % "jtidy" % "r938" % "test"
    ),
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <url>https://github.com/foundweekends/knockoff</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>https://opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:foundweekends/knockoff.git</url>
        <connection>scm:git:git@github.com:foundweekends/knockoff.git</connection>
      </scm>
      <developers>
        <developer>
          <id>xuwei-k</id>
          <name>Kenji Yoshida</name>
          <url>http://github.com/xuwei-k</url>
        </developer>
        <developer>
          <id>tjuricek</id>
          <name>Tristan Juricek</name>
          <url>http://tristanjuricek.com</url>
        </developer>
      </developers>
    ),
    parserCombinatorsVersion := "1.1.2",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %%% "scala-xml" % "2.0.0-M1",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % parserCombinatorsVersion.value
    )
  )
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/foundweekends/knockoff/" + tagOrHash.value
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
  )
  .jvmSettings(
  )

val jvm = knockoff.jvm
val js = knockoff.js

lazy val notPublish = Seq(
  skip in publish := true,
  publishArtifact := false,
  publish := {},
  publishLocal := {},
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {}
)

notPublish
