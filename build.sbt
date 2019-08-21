name               := "AnemoneActiniaria"
version            := "0.7.0-SNAPSHOT"
organization       := "de.sciss"
scalaVersion       := "2.12.9"
licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt"))
homepage           := Some(url(s"https://github.com/Sciss/${name.value}"))

lazy val wolkenpumpeVersion     = "2.35.0-SNAPSHOT"
lazy val soundProcessesVersion  = "3.31.0-SNAPSHOT"
lazy val subminVersion          = "0.2.5"
lazy val lucreVersion           = "3.14.0-SNAPSHOT"
lazy val ugenVersion            = "1.19.5"
lazy val negatumVersion         = "0.9.0-SNAPSHOT"

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
  "de.sciss" %% "negatum-core"                % negatumVersion,
  "de.sciss" %% "scalacolliderugens-core"     % ugenVersion,    // (sbt bug)
  "de.sciss" %  "scalacolliderugens-spec"     % ugenVersion,    // (sbt bug)
  "de.sciss" %% "scalacolliderugens-plugins"  % ugenVersion
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_")

updateOptions := updateOptions.value.withLatestSnapshots(false)

// ---- assembly ----

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

// ---- build info ----

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "de.sciss.anemone"

buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
  BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
  BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
)

buildInfoOptions += BuildInfoOption.BuildTime

