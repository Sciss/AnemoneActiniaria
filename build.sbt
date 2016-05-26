name               := "AnemoneActiniaria"

version            := "0.2.0-SNAPSHOT"

organization       := "de.sciss"

scalaVersion       := "2.11.8"

licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt"))

homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))

lazy val wolkenpumpeVersion   = "2.5.0"
lazy val subminVersion        = "0.2.0"
lazy val lucreVersion         = "3.3.1"

resolvers          += "Oracle Repository" at "http://download.oracle.com/maven"  // required for sleepycat

libraryDependencies ++= Seq(
  "de.sciss" %% "wolkenpumpe" % wolkenpumpeVersion,
  "de.sciss" %  "submin"      % subminVersion,
  "de.sciss" %% "lucre-bdb"   % lucreVersion
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture")

jarName in assembly := s"${name.value}.jar"
target  in assembly := baseDirectory.value
