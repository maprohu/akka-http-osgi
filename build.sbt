import java.util.jar.Attributes

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter

import scala.xml.{NodeSeq, XML}

val githubRepo = "scalajs-jsdocgen"

val akkaStreamVersion = "1.0"
val akkaVersion = "2.3.12"
val typesafeConfigVersion = "1.2.1"
val osgiVersion = "5.0.0"
val scalarxVersion = "0.2.8"

lazy val featuresXml = TaskKey[File]("features-xml")

lazy val publishSettings = Seq(
  organization := "com.github.maprohu",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
//      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      Some(sbtglobal.SbtGlobals.prodRepo)
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
  crossPaths := false
)

lazy val commonSettings = publishSettings ++ Seq(
  OsgiKeys.additionalHeaders ++= Map(
    "-noee" -> "true",
    Attributes.Name.IMPLEMENTATION_VERSION.toString -> version.value
  ),
  publishArtifact in packageDoc := false,
  OsgiKeys.exportPackage := Seq(name.value.replaceAll("-", ".")),
  OsgiKeys.privatePackage := OsgiKeys.exportPackage.value.map(_ + ".impl"),
  OsgiKeys.bundleActivator := Some(OsgiKeys.privatePackage.value(0) + ".Activator"),
  libraryDependencies ++= Seq(
    "org.osgi" % "org.osgi.core" % osgiVersion % Provided
  )
)

val noPublish = Seq(
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
)

lazy val deps = project
  .enablePlugins(SbtOsgi)
  .settings(
    commonSettings,
    osgiSettings,
    name := "akkaosgi-deps",
    version := "0.1.0",
    libraryDependencies ++= Seq(
//      "com.lihaoyi" %% "scalarx" % "0.2.8"
    ),
    OsgiKeys.bundleActivator := None,
    OsgiKeys.privatePackage := Seq(),
    OsgiKeys.explodedJars := (update in Compile).value
      .filter(moduleFilter(-"org.scala-lang"))
      .allFiles,
    OsgiKeys.exportPackage := Seq(
//      "rx.*"
    ),
    excludeDependencies ++= Seq(
      SbtExclusionRule("jline"),
      SbtExclusionRule("org.scala-lang.modules"),
      SbtExclusionRule("org.osgi")
    )

  )

lazy val system = project
  .enablePlugins(SbtOsgi)
  .settings(
    commonSettings,
    osgiSettings,
    version := "0.1.2",
    description := "AkkaOsgi System",
    name := "akkaosgi-system",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-osgi" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
      "com.github.maprohu" %% "scalarx" % scalarxVersion
    )
  )

lazy val routeSample = project
  .enablePlugins(SbtOsgi)
  .dependsOn(system)
  .settings(
    commonSettings,
    osgiSettings,
    name := "akkaosgi-route-sample"
  )



lazy val features = project
  .settings(
    publishSettings,
    name := "akkaosgi-features",
    version := "0.0.0-SNAPSHOT",
    publishArtifact := false,
    publishTo := Some(sbtglobal.SbtGlobals.prodRepo),

    featuresXml := Def.task {
      type PRef = (String, String, String)
      def ref(p: PRef) =
        <bundle>mvn:{p._1}/{p._2}/{p._3}</bundle>
      def feat(p: PRef, refs: PRef*)(inner: NodeSeq = Seq()) =
        <feature name={p._2} version={p._3}>
          {refs.map(r => <feature version={r._3}>{r._2}</feature>)}
          {inner}
          {ref(p)}
        </feature>

      val depsRef = ((organization in deps).value, (name in deps).value, (version in deps).value)
      val systemRef = ((organization in system).value, (name in system).value, (version in system).value)
      val routeSampleRef = ((organization in routeSample).value, (name in routeSample).value, (version in routeSample).value)

      val f = target.value / "features.xml"
      f.getParentFile.mkdirs()
      val node =
        <features name="akkaosgi" xmlns="http://karaf.apache.org/xmlns/features/v1.3.0">
          {feat(depsRef)(
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
            <bundle>mvn:com.github.maprohu/scalarx_2.11/{scalarxVersion}</bundle>
          )}
          {feat(systemRef, depsRef)()}
          {feat(routeSampleRef, systemRef)()}
        </features>

      XML.save(f.absolutePath, node, "UTF-8", true)
      f
    }.value,

    addArtifact( Artifact("akkaosgi-features", "features", "xml"), featuresXml )
  )


lazy val root = (project in file("."))
  .aggregate(system, routeSample)
  .settings(
    noPublish
  )
