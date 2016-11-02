name := """root"""

version := "1.0-SNAPSHOT"

lazy val `root` = (project in file(".")).enablePlugins(PlayScala)
//
scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  javaJdbc,
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.postgresql" % "postgresql" % "9.4.1211",
  "org.scalikejdbc" %% "scalikejdbc" % "2.4.1",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.4.1",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.1",
  "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.5.1",
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "2.4.1"
)

