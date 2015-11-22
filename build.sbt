val akkaStreamV = "2.0-M1"
val akkaV = "2.4.0"

lazy val root = (project in file("."))
  .enablePlugins(SbtOsgi)
  .settings(
    name := "osgiapp",
    organization := "osgiapp",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    crossPaths := false,
    publishTo := Some(Resolver.file("repo", new File("/opt/repo"))),

    libraryDependencies ++=
      Seq(
        //    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
        //    "com.typesafe.akka" %% "akka-osgi"               % akkaV
        "org.osgi" % "org.osgi.core" % "5.0.0" % Provided

      ),
    OsgiKeys.bundleSymbolicName := "osgiapp",
    OsgiKeys.bundleActivator := Some("osgiapp.Activator"),
    osgiSettings
  )

