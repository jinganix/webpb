import com.google.protobuf.gradle.protobuf
import org.apache.tools.ant.taskdefs.condition.Os
import utils.which

plugins {
  id("ts.frontend")
}

dependencies {
  protobuf(project(":sample:proto"))
}

val npm = if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd" else project.which("npm")

task<Exec>("npmInstall") {
  val nodeModules = file("./node_modules")
  if (nodeModules.exists()) {
    commandLine(npm, "--version")
  } else {
    commandLine(npm, "install", "--verbose")
  }
}

task<Exec>("npmStart") {
  commandLine(npm, "run", "dev")

  dependsOn("npmInstall")
  dependsOn("generateProto")
}

task<Exec>("npmCheck") {
  commandLine(npm, "run", "lint")
  commandLine(npm, "run", "test")

  dependsOn("npmInstall")
  dependsOn("generateProto")
}

tasks.check {
  dependsOn("npmCheck")
}
