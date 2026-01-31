import org.apache.tools.ant.taskdefs.condition.Os
import utils.npmInstallTask
import utils.which

plugins {
  id("ts.runtime")
}

npmInstallTask()

val npm = if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd" else which("npm") ?: "npm"

tasks.register<Exec>("npmStart") {
  commandLine(npm, "run", "dev")

  dependsOn("npmInstall")
}

tasks.register<Exec>("npmCheck") {
  commandLine(npm, "run", "lint")
  commandLine(npm, "run", "test")

  dependsOn("npmInstall")
}

tasks.check {
  dependsOn("npmCheck")
}
