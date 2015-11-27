import java.util.jar.Attributes

val githubRepo = "scalajs-jsdocgen"

val akkaStreamVersion = "1.0"
val akkaVersion = "2.3.12"
val typesafeConfigVersion = "1.2.1"
val osgiVersion = "5.0.0"


lazy val features = TaskKey[File]("features")

lazy val commonSettings = Seq(
  organization := "com.github.maprohu",
  version := "0.1.1-SNAPSHOT",
  resolvers += Resolver.sonatypeRepo("snapshots"),
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
    name := "akkaosgi-system",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-osgi" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
      "com.github.maprohu" %% "scalarx" % "0.2.8-SNAPSHOT"
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

//lazy val app = project
//  .enablePlugins(SbtOsgi)
//  .settings(
//    osgiSettings,
//    commonSettings,
//    name := appName,
//
//    libraryDependencies ++=
//      Seq(
//        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
//        "com.typesafe.akka" %% "akka-osgi" % akkaVersion,
//        "org.osgi" % "org.osgi.core" % osgiVersion % Provided,
//        "org.osgi" % "org.osgi.compendium" % osgiVersion % Provided
//        //        "org.apache.karaf.bundle" % "org.apache.karaf.bundle.core" % "4.0.3" % Provided
//      ),
//    OsgiKeys.bundleSymbolicName := name.value,
//    OsgiKeys.bundleActivator := Some(s"$basePackage.Activator"),
//    OsgiKeys.privatePackage := Seq(
//      basePackage
//    ),
//    OsgiKeys.exportPackage ++= Seq(
//      s"$basePackage.service"
//    ),
//
//    features := Def.task {
//      val f = target.value / "features.xml"
//      val node =
//        <features name={name.value} xmlns="http://karaf.apache.org/xmlns/features/v1.3.0">
//          <feature name={name.value} version={version.value}>
//            <bundle>mvn:org.scala-lang/scala-library/{scalaVersion.value}</bundle>
//            <bundle>mvn:org.scala-lang/scala-reflect/{scalaVersion.value}</bundle>
//            <bundle>mvn:org.reactivestreams/reactive-streams/1.0.0</bundle>
//            <bundle>mvn:com.typesafe/config/{typesafeConfigVersion}</bundle>
//            <bundle>mvn:com.typesafe.akka/akka-osgi_2.11/{akkaVersion}</bundle>
//            <bundle>mvn:com.typesafe.akka/akka-actor_2.11/{akkaVersion}</bundle>
//            <bundle>mvn:com.typesafe.akka/akka-stream-experimental_2.11/{akkaStreamVersion}</bundle>
//            <bundle>mvn:com.typesafe.akka/akka-http-experimental_2.11/{akkaStreamVersion}</bundle>
//            <bundle>mvn:com.typesafe.akka/akka-http-core-experimental_2.11/{akkaStreamVersion}</bundle>
//            <bundle>mvn:com.typesafe.akka/akka-parsing-experimental_2.11/{akkaStreamVersion}</bundle>
//            <bundle>mvn:{organization.value}/{name.value}/{version.value}</bundle>
//          </feature>
//        </features>
//
//      XML.save(f.absolutePath, node, "UTF-8", true)
//      f
//    }.value,
//
//    addArtifact( Artifact(appName, "features", "xml", "features"), features )
//  )


lazy val root = (project in file("."))
  .aggregate(system, routeSample)
  .settings(
    noPublish
  )
