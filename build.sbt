name               := "AnemoneActiniaria"
version            := "0.5.0-SNAPSHOT"
organization       := "de.sciss"
scalaVersion       := "2.12.4"
licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt"))
homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))

lazy val wolkenpumpeVersion    = "2.20.0"
lazy val soundProcessesVersion = "3.16.0"
lazy val subminVersion         = "0.2.2"
lazy val lucreVersion          = "3.5.0"

resolvers          += "Oracle Repository" at "http://download.oracle.com/maven"  // required for sleepycat

libraryDependencies ++= Seq(
  "de.sciss" %% "wolkenpumpe"         % wolkenpumpeVersion,
  "de.sciss" %% "soundprocesses-core" % soundProcessesVersion,
  "de.sciss" %  "submin"              % subminVersion,
  "de.sciss" %% "lucre-core"          % lucreVersion,
  "de.sciss" %% "lucre-bdb"           % lucreVersion
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_")

assemblyJarName in assembly := s"${name.value}.jar"
target          in assembly := baseDirectory.value

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.last
  case PathList("org", "xmlpull", _ @ _*)              => MergeStrategy.first
  case PathList("org", "w3c", "dom", "events", _ @ _*) => MergeStrategy.first // bloody Apache Batik
  case x =>
    val old = (assemblyMergeStrategy in assembly).value
    old(x)
}
