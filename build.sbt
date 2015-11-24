import java.io.FileWriter
import java.util.jar.Attributes

import sbt.Package.ManifestAttributes

import scala.xml.XML

val githubRepo = "scalajs-jsdocgen"

val akkaStreamVersion = "1.0"
val akkaVersion = "2.3.12"
val typesafeConfigVersion = "1.2.1"
val osgiVersion = "5.0.0"
val basePackage = "com.github.maprohu.httposgi"


lazy val appName = "akka-http-osgi"

lazy val features = TaskKey[File]("features")

lazy val commonSettings = Seq(
  organization := "com.github.maprohu",
  version := "0.1.1-SNAPSHOT",
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false },
  licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")),
  homepage := Some(url(s"https://github.com/maprohu/${githubRepo}")),
  pomExtra := (
    <scm>
      <url>git@github.com:maprohu/{githubRepo}.git</url>
      <connection>scm:git:git@github.com:maprohu/{githubRepo}.git</connection>
    </scm>
      <developers>
        <developer>
          <id>maprohu</id>
          <name>maprohu</name>
          <url>https://github.com/maprohu</url>
        </developer>
      </developers>
    ),

  scalaVersion := "2.11.7",
  crossPaths := false,
  OsgiKeys.additionalHeaders ++= Map(
    "-noee" -> "true",
    Attributes.Name.IMPLEMENTATION_VERSION.toString -> version.value
  ),
  publishArtifact in packageDoc := false

//  packageOptions += Package.ManifestAttributes((Attributes.Name.IMPLEMENTATION_VERSION, version.value))

)

val noPublish = Seq(
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
)

lazy val app = project
  .enablePlugins(SbtOsgi)
  .settings(
    osgiSettings,
    commonSettings,
    name := appName,

    libraryDependencies ++=
      Seq(
        "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-osgi"               % akkaVersion,
        "org.osgi" % "org.osgi.core" % osgiVersion % Provided,
        "org.osgi" % "org.osgi.compendium" % osgiVersion % Provided
        //        "org.apache.karaf.bundle" % "org.apache.karaf.bundle.core" % "4.0.3" % Provided
      ),
    OsgiKeys.bundleSymbolicName := name.value,
    OsgiKeys.bundleActivator := Some(s"$basePackage.Activator"),
    OsgiKeys.privatePackage := Seq(
      basePackage
    ),
    OsgiKeys.exportPackage ++= Seq(
      s"$basePackage.service"
    ),

    features := Def.task {
      val f = target.value / "features.xml"
      val node =
        <features name={name.value} xmlns="http://karaf.apache.org/xmlns/features/v1.3.0">
          <feature name={name.value} version={version.value}>
            <bundle>mvn:org.scala-lang/scala-library/{scalaVersion.value}</bundle>
            <bundle>mvn:org.scala-lang/scala-reflect/{scalaVersion.value}</bundle>
            <bundle>mvn:org.reactivestreams/reactive-streams/1.0.0</bundle>
            <bundle>mvn:com.typesafe/config/{typesafeConfigVersion}</bundle>
            <bundle>mvn:com.typesafe.akka/akka-osgi_2.11/{akkaVersion}</bundle>
            <bundle>mvn:com.typesafe.akka/akka-actor_2.11/{akkaVersion}</bundle>
            <bundle>mvn:com.typesafe.akka/akka-stream-experimental_2.11/{akkaStreamVersion}</bundle>
            <bundle>mvn:com.typesafe.akka/akka-http-experimental_2.11/{akkaStreamVersion}</bundle>
            <bundle>mvn:com.typesafe.akka/akka-http-core-experimental_2.11/{akkaStreamVersion}</bundle>
            <bundle>mvn:com.typesafe.akka/akka-parsing-experimental_2.11/{akkaStreamVersion}</bundle>
            <bundle>mvn:{organization.value}/{name.value}/{version.value}</bundle>
          </feature>
        </features>

      XML.save(f.absolutePath, node, "UTF-8", true)
      f
    }.value,

    addArtifact( Artifact(appName, "features", "xml", "features"), features )
  )

lazy val sample = project
  .enablePlugins(SbtOsgi)
  .dependsOn(app)
  .settings(
    osgiSettings,
    commonSettings,
    name := appName + "-sample",

    OsgiKeys.privatePackage := Seq(
      s"$basePackage.sample"
    ),
    OsgiKeys.bundleSymbolicName := name.value,
    OsgiKeys.bundleActivator := Some(s"$basePackage.sample.Activator")
  )



lazy val root = (project in file("."))
  .aggregate(app, sample)
  .settings(
    noPublish
  )
