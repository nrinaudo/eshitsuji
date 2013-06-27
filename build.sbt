// - Project metadata --------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
name := "eShitsuji"

version := "1.0"

scalaVersion := "2.9.2"



// - Compilation -------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// Enables explicit deprecation warnings.
scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

// Removes the scala version number from published artifacts.
crossPaths := false



// - Execution ---------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
mainClass := Some("com.nrinaudo.eshitsuji.Launcher")

// This is necessary: eshitsuji uses a native library (sqlite) that starts misbehaving if loaded more than once in the
// same VM.
fork := true



// - Plugins -----------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
org.scalastyle.sbt.ScalastylePlugin.Settings

org.scalastyle.sbt.PluginKeys.config <<= baseDirectory {_ / "project" / "scalastyle-config.xml"}



// - Dependencies ------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
libraryDependencies += "nekohtml"         %  "nekohtml"     % "1.9.6.2"

libraryDependencies += "org.scribe"       %  "scribe"       % "1.3.3"

libraryDependencies += "org.xerial"       %  "sqlite-jdbc"  % "3.7.2"

libraryDependencies += "com.github.scopt" %% "scopt"        % "2.1.0"

libraryDependencies += "org.scalatest"    %% "scalatest"    % "1.9.1" % "test"

libraryDependencies += "org.scalacheck"   %% "scalacheck"   % "1.10.1" % "test"
