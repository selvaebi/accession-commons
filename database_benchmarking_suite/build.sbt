name := "database_benchmarking_suite"

version := "0.1"

scalaVersion := "2.12.4"

// https://mvnrepository.com/artifact/org.apache.jmeter/ApacheJMeter_core
libraryDependencies += "org.apache.jmeter" % "ApacheJMeter_core" % "3.3"
// https://mvnrepository.com/artifact/org.apache.jmeter/ApacheJMeter_components
libraryDependencies += "org.apache.jmeter" % "ApacheJMeter_components" % "3.3"
// https://mvnrepository.com/artifact/org.apache.jmeter/ApacheJMeter_java
libraryDependencies += "org.apache.jmeter" % "ApacheJMeter_java" % "3.3"
// https://mvnrepository.com/artifact/com.datastax.cassandra/cassandra-driver-core
libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.4.0"
//Scallop Command-line parsing library
libraryDependencies += "org.rogach" %% "scallop" % "3.1.1"
//MongoDB library
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.0"
//Postgres library
libraryDependencies ++= List("org.postgresql" % "postgresql" % "9.4.1212","com.typesafe.slick" %% "slick" % "3.2.1")
//Config library
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.9.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.last
}