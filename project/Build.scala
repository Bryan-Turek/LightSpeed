import sbt._
import sbt.Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, additionalLibs}

object AkkaClusterTestappBuild extends Build {

  val akkaVersion = "2.2.1"

  lazy val akkaOpsworks = Project(
    id = "akka-clustering",
    base = file("."),
    settings = Project.defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      name := "akka-clustering",
      organization := "LightSpeed",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.2",
      unmanagedJars in Compile := (baseDirectory.value ** "*.jar").classpath,
      scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
      javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation"),
      // this is only needed while we use timestamped snapshot version of akka   
      mainClass in (Compile, run) := Some("cluster.Main"),
      distJvmOptions in Dist := "-Djava.library.path=./sigar -Xms256M -Xmx1024M -XX:-HeapDumpOnOutOfMemoryError",
      additionalLibs in Dist := file("sigar").listFiles.filter(f => !f.isDirectory)
    )
  )
}