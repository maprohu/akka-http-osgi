import java.io.FileWriter

import scala.xml.XML

val akkaStreamVersion = "1.0"
val akkaVersion = "2.3.12"
val typesafeConfigVersion = "1.2.1"
val osgiVersion = "5.0.0"


lazy val appName = "osgiapp"

lazy val features = TaskKey[File]("features")

lazy val commonSettings = Seq(
  organization := appName,
  version := "1.2-SNAPSHOT",
  scalaVersion := "2.11.7",
  crossPaths := false,
  OsgiKeys.additionalHeaders += ("-noee" -> "true"),
  publishArtifact in packageDoc := false
)


lazy val app = project
  .enablePlugins(SbtOsgi)
  .settings(
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
    osgiSettings,
    OsgiKeys.bundleSymbolicName := name.value,
    OsgiKeys.bundleActivator := Some("osgiapp.Activator"),
    OsgiKeys.exportPackage ++= Seq(
      "osgiapp.service"
    ),

    features := Def.task {
      val f = target.value / "features.xml"
      val node =
        <features name={name.value} xmlns="http://karaf.apache.org/xmlns/features/v1.3.0">
          <feature name={name.value} version="1.0.0.SNAPSHOT">
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
    commonSettings,
    name := appName + "-sample",
    version := "1.2-SNAPSHOT",

    osgiSettings,
    OsgiKeys.privatePackage := Seq(
      "osgiapp.sample"
    ),
    OsgiKeys.bundleSymbolicName := name.value,
    OsgiKeys.bundleActivator := Some("osgiapp.sample.Activator")
  )



lazy val root = (project in file("."))
  .aggregate(app, sample)
  .settings(
    publishArtifact := false
  )
