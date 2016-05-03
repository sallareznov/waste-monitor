name := "waste-monitor"
version := "1.0"
scalaVersion := "2.11.8"
sbtVersion := "0.13.11"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers ++= Seq(
  "Typesafe repository" at "https://dl.bintray.com/typesafe/maven-releases/",
  Resolver.jcenterRepo,
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0",
  "org.mockito" % "mockito-all" % "1.10.19",
  "com.github.t3hnar" % "scala-bcrypt_2.11" % "2.6",
  "commons-codec" % "commons-codec" % "1.10",
  "com.github.wookietreiber" % "scala-chart_2.11" % "0.5.0",
  "org.jfree" % "jfreechart" % "1.0.19",
  "com.iheart" %% "play-swagger" % "0.2.5-PLAY2.5",
  "org.webjars" % "swagger-ui" % "2.1.4",
  "io.kanaka" %% "play-monadic-actions" % "1.1.0"
  //"org.scalaz" % "scalaz-core_2.11" % "7.1.0"
)
