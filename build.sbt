import org.nlogo.build.{ ExtensionDocumentationPlugin, NetLogoExtension }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

scalaVersion := "2.12.0"

netLogoExtName := "file"
netLogoVersion := "6.0.2"
netLogoClassManager := "org.nlogo.extensions.file.FileExtension"
netLogoZipSources := false

netLogoTarget := NetLogoExtension.directoryTarget(baseDirectory.value)

scalaSource in Compile := baseDirectory.value / "src" / "main"
scalaSource in Test := baseDirectory.value / "src" / "test"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.picocontainer" % "picocontainer" % "2.13.6" % "test",
  "org.parboiled" %% "parboiled" % "2.1.3",
  "org.ow2.asm" % "asm-all" % "5.0.3" % "test",
  "commons-codec" % "commons-codec" % "1.10" % "test"
)

val moveToFileDir = taskKey[Unit]("add all resources to File directory")

val fileDirectory = settingKey[File]("directory that extension is moved to for testing")

fileDirectory := baseDirectory.value / "extensions" / "file"

moveToFileDir := {
  (packageBin in Compile).value
  val testTarget = NetLogoExtension.directoryTarget(fileDirectory.value)
  testTarget.create(NetLogoExtension.netLogoPackagedFiles.value)/*
  val testResources = (baseDirectory.value / "test" ***).filter(_.isFile)
  for (file <- testResources.get)
    IO.copyFile(file, fileDirectory.value / "test" / IO.relativize(baseDirectory.value / "test", file).get)*/
}

test in Test := {
  IO.createDirectory(fileDirectory.value)
  moveToFileDir.value
  (test in Test).value
  IO.delete(fileDirectory.value)
}

