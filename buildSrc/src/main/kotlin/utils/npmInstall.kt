package utils

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

fun Project.npmInstallTask() {
  val npmCommand by lazy {
    if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd"
    else which("npm") ?: "npm"
  }

  tasks.register<Exec>("npmInstall") {
    group = "build"
    description =
        "Installs npm dependencies (or shows version if already installed). Use -Pforce=true to force install."

    val nodeModulesFile = layout.projectDirectory.file("node_modules")
    val forceInstallProvider = providers.gradleProperty("force").map { it == "true" }

    doFirst {
      val forceInstall = forceInstallProvider.getOrElse(false)
      val nodeModules = nodeModulesFile.asFile

      val args =
          if (forceInstall || !nodeModules.exists()) {
            listOf("install", "--verbose")
          } else {
            listOf("--version")
          }

      println("Running npm ${args.joinToString(" ")} (force=$forceInstall)")
      commandLine(npmCommand, *args.toTypedArray())
    }
  }
}
