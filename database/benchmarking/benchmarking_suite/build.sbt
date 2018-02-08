name := "scala-benchmarking-test"

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

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}