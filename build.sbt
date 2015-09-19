name               := "AnemoneActiniaria"

version            := "0.1.0-SNAPSHOT"

organization       := "de.sciss"

scalaVersion       := "2.11.7"

licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt"))

homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))

lazy val wolkenpumpeVersion   = "2.1.0-SNAPSHOT"
lazy val webLaFVersion        = "1.28"
lazy val lucreVersion         = "3.2.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "de.sciss" %% "wolkenpumpe" % wolkenpumpeVersion,
  "de.sciss" %  "weblaf"      % webLaFVersion,
  "de.sciss" %% "lucre-bdb"   % lucreVersion
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture")

jarName in assembly := s"${name.value}.jar"
target  in assembly := baseDirectory.value
