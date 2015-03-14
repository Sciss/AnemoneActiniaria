name               := "AnemoneActiniaria"

version            := "0.1.0-SNAPSHOT"

organization       := "de.sciss"

scalaVersion       := "2.11.6"

licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt"))

homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))

lazy val wolkenpumpeVersion   = "1.2.0"

lazy val webLaFVersion        = "1.28"

libraryDependencies ++= Seq(
  "de.sciss" %% "wolkenpumpe" % wolkenpumpeVersion,
  "de.sciss" %  "weblaf"      % webLaFVersion
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture")

jarName in assembly := s"${name.value}.jar"

target in assembly := baseDirectory.value
