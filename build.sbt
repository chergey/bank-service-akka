name := "bank-service-akka"
organization := "org.elcer"
version := "1.0.0"
scalaVersion := "2.13.0"

libraryDependencies ++= {
  val akkaHttpV = "10.1.8"
  val scalaTestV = "3.0.5"
  val slickVersion = "3.3.2"
  val circeV = "0.12.0-M4"
  val sttpV = "1.6.3"
  Seq(
    // HTTP server
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,

    "com.typesafe.akka" %% "akka-actor" % "2.6.0-M4",
    "com.typesafe.akka" %% "akka-stream" % "2.6.0-M4",

  // Support of CORS requests, version depends on akka-http
    "ch.megard" %% "akka-http-cors" % "0.4.1",

    // SQL generator
    "com.typesafe.slick" %% "slick" % slickVersion,

    // Postgres driver
    "org.postgresql" % "postgresql" % "42.2.5",

    // Migration for SQL databases
    "org.flywaydb" % "flyway-core" % "5.1.4",

    // Connection pool for database
    "com.zaxxer" % "HikariCP" % "2.7.0",

    // Encoding decoding sugar, used in passwords hashing
 //   "com.roundeights" % "hasher_2.12" % "1.2.0",

    // Parsing and generating of JWT tokens
    "com.pauldijou" %% "jwt-core" % "3.1.0",

  // Config file parser
    "com.github.pureconfig" %% "pureconfig" % "0.11.1",

    // JSON serialization library
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,

    // Validation library
//    "com.wix" %% "accord-core" % "0.7.1",

    // Http client, used currently only for IT test
    "com.softwaremill.sttp" %% "core" % sttpV % Test,
    "com.softwaremill.sttp" %% "akka-http-backend" % sttpV % Test,

    "org.scalatest" % "scalatest_2.13.0-M2" % scalaTestV % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
    "ru.yandex.qatools.embed" % "postgresql-embedded" % "2.4" % Test,

    "org.apache.derby" % "derby" % "10.14.2.0" ,

    "org.mockito" % "mockito-all" % "1.9.5" % Test

  )
}

enablePlugins(UniversalPlugin)
enablePlugins(DockerPlugin)

// Needed for Heroku deployment, can be removed
enablePlugins(JavaAppPackaging)
