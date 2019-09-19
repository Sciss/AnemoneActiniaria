lazy val commonSettings = Seq(
  name               := "AnemoneActiniaria",
  version            := "0.7.0-SNAPSHOT",
  organization       := "de.sciss",
  scalaVersion       := "2.12.10",
  licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  homepage           := Some(url(s"https://github.com/Sciss/${name.value}")),
  resolvers          += "Oracle Repository" at "http://download.oracle.com/maven",  // required for sleepycat
  scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint:-stars-align,_"),
  scalacOptions      += "-Yrangepos",  // this is needed to extract source code
  updateOptions      := updateOptions.value.withLatestSnapshots(false)
)

lazy val deps = new {
  val fscape          = "2.30.0"
  val lucre           = "3.15.0"
  val negatum         = "0.9.0"
  val soundProcesses  = "3.32.0"
  val submin          = "0.2.5"
  val ugen            = "1.19.5"
  val wolkenpumpe     = "2.37.0-SNAPSHOT"
}

lazy val mainCl = "de.sciss.anemone.Anemone"

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := s"${name.value}.jar",
  target          in assembly := baseDirectory.value,
  mainClass       in assembly := Some(mainCl),
  assemblyMergeStrategy in assembly := {
    case "logback.xml" => MergeStrategy.last
    case PathList("org", "xmlpull", _ @ _*)              => MergeStrategy.first
    case PathList("org", "w3c", "dom", "events", _ @ _*) => MergeStrategy.first // bloody Apache Batik
    case x =>
      val old = (assemblyMergeStrategy in assembly).value
      old(x)
  }
)

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(assemblySettings)
  .settings(
    name := "AnemoneActiniaria",
    mainClass in (Compile, run) := Some(mainCl),
    fork      in           run  := true,
    libraryDependencies ++= Seq(
      "de.sciss" %% "fscape-lucre"                % deps.fscape,
      "de.sciss" %% "fscape-macros"               % deps.fscape,
      "de.sciss" %% "lucre-bdb"                   % deps.lucre,
      "de.sciss" %% "negatum-core"                % deps.negatum,
      "de.sciss" %% "scalacolliderugens-core"     % deps.ugen,    // (sbt bug)
      "de.sciss" %  "scalacolliderugens-spec"     % deps.ugen,    // (sbt bug)
      "de.sciss" %% "scalacolliderugens-plugins"  % deps.ugen,
      "de.sciss" %% "soundprocesses-core"         % deps.soundProcesses,
      "de.sciss" %  "submin"                      % deps.submin,
      "de.sciss" %% "wolkenpumpe"                 % deps.wolkenpumpe,
    ),
    buildInfoPackage := "de.sciss.anemone",
    buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
      BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
      BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
    ),
    buildInfoOptions += BuildInfoOption.BuildTime,
  )
