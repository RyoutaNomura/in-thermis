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
libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46"
libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.1"
libraryDependencies += "com.taskadapter" % "redmine-java-api" % "2.6.0"
libraryDependencies += "org.apache.poi" % "poi" % "3.14"
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.14"
libraryDependencies += "org.apache.poi" % "poi-scratchpad" % "3.14"
libraryDependencies += "com.ibm.icu" % "icu4j" % "57.1"
libraryDependencies += "org.jsoup" % "jsoup" % "1.9.1"
libraryDependencies += "jcifs" % "jcifs" % "1.3.17"
libraryDependencies += "net.databinder.dispatch" % "dispatch-core_2.11" % "0.11.3"
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"

// https://mvnrepository.com/artifact/org.sweble.wikitext/swc-engine
libraryDependencies += "org.sweble.wikitext" % "swc-engine" % "2.1.0"
libraryDependencies += "org.sweble.wikitext" % "swc-example-basic" % "2.1.0"

// https://mvnrepository.com/artifact/org.apache.tika/tika-parsers
libraryDependencies += "org.apache.tika" % "tika-parsers" % "1.13"



