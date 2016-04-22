name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

//scalacOptions += "-Ylog-classpath"

libraryDependencies ++= Seq(
//  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0"
libraryDependencies += "com.google.code" % "lucene-gosen-ipadic" % "1.2.1"
libraryDependencies += "org.apache.odftoolkit" % "simple-odf" % "0.8.1-incubating"
libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "2.0.0"
libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.1"
libraryDependencies += "com.taskadapter" % "redmine-java-api" % "2.6.0"
libraryDependencies += "org.apache.poi" % "poi" % "3.14"
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.14"
libraryDependencies += "org.apache.poi" % "poi-scratchpad" % "3.14"
libraryDependencies += "com.ibm.icu" % "icu4j" % "57.1"
libraryDependencies += "org.jsoup" % "jsoup" % "1.9.1"




