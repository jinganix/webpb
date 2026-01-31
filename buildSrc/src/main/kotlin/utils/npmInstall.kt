package utils

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.apache.tools.ant.taskdefs.condition.Os

fun Project.npmInstallTask() {
  val npmCommand by lazy {
    if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd"
    else which("npm") ?: "npm"
  }

  tasks.register<Exec>("npmInstall") {
    group = "build"
    description = "Installs npm dependencies (or shows version if already installed). Use -Pforce=true to force install."

    doFirst {
      val nodeModules = project.file("node_modules")

      val forceInstall = project.findProperty("force")?.toString()?.toBoolean() ?: false

      val args = if (forceInstall || !nodeModules.exists()) {
        listOf("install", "--verbose")
      } else {
        listOf("--version")
      }

      println("Running npm ${args.joinToString(" ")} (force=$forceInstall)")
      commandLine(npmCommand, *args.toTypedArray())
    }
  }
}
