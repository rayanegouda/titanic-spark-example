resolvers += Resolver.url("bintray-sbt-plugins", url("http://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

name := "Sample Spark Project - Titanic Data"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq( 
	"org.apache.spark" %% "spark-core" % "1.4.1"  % "provided",
	"org.apache.spark" %% "spark-mllib" % "1.4.1"  % "provided"
)
	

libraryDependencies += "org.elasticsearch" %% "elasticsearch-spark" % "2.1.1"
