name               := "AnemoneActiniaria"
version            := "0.5.0-SNAPSHOT"
organization       := "de.sciss"
scalaVersion       := "2.12.4"
licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt"))
homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))

lazy val wolkenpumpeVersion     = "2.21.1"
lazy val soundProcessesVersion  = "3.16.1"
lazy val subminVersion          = "0.2.2"
lazy val lucreVersion           = "3.5.0"
lazy val ugenVersion            = "1.17.1"

resolvers          += "Oracle Repository" at "http://download.oracle.com/maven"  // required for sleepycat

lazy val mainCl = "de.sciss.anemone.Anemone"

mainClass in (Compile, run) := Some(mainCl)
fork      in           run  := true

libraryDependencies ++= Seq(
  "de.sciss" %% "wolkenpumpe"                 % wolkenpumpeVersion,
  "de.sciss" %% "soundprocesses-core"         % soundProcessesVersion,
  "de.sciss" %  "submin"                      % subminVersion,
  "de.sciss" %% "lucre-core"                  % lucreVersion,
  "de.sciss" %% "lucre-bdb"                   % lucreVersion,
  "de.sciss" %% "scalacolliderugens-plugins"  % ugenVersion
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_")

assemblyJarName in assembly := s"${name.value}.jar"
target          in assembly := baseDirectory.value
mainClass       in assembly := Some(mainCl)

assemblyMergeStrategy in assembly := {
  case "logback.xml" => MergeStrategy.last
  case PathList("org", "xmlpull", _ @ _*)              => MergeStrategy.first
  case PathList("org", "w3c", "dom", "events", _ @ _*) => MergeStrategy.first // bloody Apache Batik
  case x =>
    val old = (assemblyMergeStrategy in assembly).value
    old(x)
}
