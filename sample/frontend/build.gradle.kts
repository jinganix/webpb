import com.google.protobuf.gradle.protobuf
import org.apache.tools.ant.taskdefs.condition.Os
import utils.npmInstallTask
import utils.which

plugins {
  id("ts.frontend")
}

dependencies {
  protobuf(project(":sample:proto"))
}

npmInstallTask()

val npm = if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd" else which("npm") ?: "npm"

tasks.register<Exec>("npmStart") {
  commandLine(npm, "run", "dev")

  dependsOn("npmInstall")
  dependsOn("generateProto")
}

tasks.register<Exec>("npmCheck") {
  commandLine(npm, "run", "lint")
  commandLine(npm, "run", "test")

  dependsOn("npmInstall")
  dependsOn("generateProto")
}

tasks.check {
  dependsOn("npmCheck")
}
