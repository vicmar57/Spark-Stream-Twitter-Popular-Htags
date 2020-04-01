name := "SparkStreamTwitt"

version := "0.1"

scalaVersion := "2.12.1"

val sparkVersion = "2.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.bahir" %% "spark-streaming-twitter" % sparkVersion,
  "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0"
)