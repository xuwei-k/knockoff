import sbtrelease.ReleaseStateTransformations._

val tagName = Def.setting{
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

releaseTagName := tagName.value

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
  pushChanges
)

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "knockoff"

buildInfoObject := "KnockoffBuildInfo"

name:="knockoff"

val Scala212 = "2.12.1"

scalaVersion := Scala212

crossScalaVersions := Seq("2.11.8", Scala212)

organization := "org.foundweekends"

val tagOrHash = Def.setting {
  if(isSnapshot.value) sys.process.Process("git rev-parse HEAD").lines_!.head
  else tagName.value
}

scalacOptions in (Compile, doc) ++= {
  val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
  Seq("-sourcepath", base, "-doc-source-url", "https://github.com/foundweekends/knockoff/tree/" + tagOrHash.value + "€{FILE_PATH}.scala")
}

val unusedWarnings = Seq(
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

scalacOptions ++= unusedWarnings

scalacOptions ++= Seq("-language:_", "-deprecation", "-Xfuture", "-Xlint")

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "junit" % "junit" % "4.12" % "test",
  "net.sf.jtidy" % "jtidy" % "r938" % "test"
)

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value ++ Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
      )
    case _ =>
      libraryDependencies.value 
  }
}

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/foundweekends/knockoff</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
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
)
