/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/jinganix/webpb
 */

package utils

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.register

data class ProtocPluginNativeBuild(
  val buildNativeBinaries: TaskProvider<Task>,
  val platformExecutables: Map<String, Provider<RegularFile>>,
  val nativeResourceDir: Provider<Directory>,
)

fun Project.configureProtocPluginNative(
  goBuildTaskPath: String,
  goCmdPackage: String,
  binaryName: String,
): ProtocPluginNativeBuild {
  val pluginDir = rootProject.layout.projectDirectory.dir("plugin")
  val nativeDir = layout.buildDirectory.dir("native")
  val nativeResourceDir = layout.buildDirectory.dir("generated/native-resources")
  val buildAllNativePlatforms =
    providers
      .gradleProperty("buildAllNativePlatforms")
      .map { it.equals("true", ignoreCase = true) }
      .orElse(false)

  fun shouldBuildAllPlatforms(): Boolean {
    return buildAllNativePlatforms.get() ||
      providers.environmentVariable("GITHUB_ACTIONS").getOrElse("false") == "true"
  }

  val hostPlatform = ProtocPlatforms.hostPlatform()
  val hostJarBinaryName = ProtocPlatforms.jarBinaryName(binaryName, hostPlatform)
  val pluginExt = if (OperatingSystem.current().isWindows) ".exe" else ""

  val buildHostNativeBinary =
    tasks.register<Copy>("buildHostNativeBinary") {
      group = "build"
      description = "Build host native binary for $binaryName"
      dependsOn(goBuildTaskPath)
      from(pluginDir.file("bin/$binaryName$pluginExt"))
      into(nativeResourceDir.map { it.dir("native/${hostPlatform.classifier}") })
      rename { hostJarBinaryName }
      doLast {
        val nativeOutput =
          nativeDir.get().file("${hostPlatform.classifier}/$hostJarBinaryName").asFile
        nativeOutput.parentFile.mkdirs()
        pluginDir.file("bin/$binaryName$pluginExt").asFile.copyTo(nativeOutput, overwrite = true)
      }
    }

  val platformTasks = linkedMapOf<String, TaskProvider<Exec>>()

  ProtocPlatforms.all.forEach { platform ->
    val jarBinaryName = ProtocPlatforms.jarBinaryName(binaryName, platform)
    val nativeOutput = nativeDir.map { it.file("${platform.classifier}/$jarBinaryName") }

    val buildTask =
      tasks.register<Exec>("buildNativeBinary_${platform.classifier}") {
        group = "build"
        description = "Cross-compile $binaryName for ${platform.classifier}"
        onlyIf { shouldBuildAllPlatforms() }
        outputs.file(nativeOutput)
        workingDir = pluginDir.asFile
        doFirst {
          val output = nativeOutput.get().asFile
          output.parentFile.mkdirs()
          environment(
            mapOf(
              "GOOS" to platform.goOs,
              "GOARCH" to platform.goArch,
              "CGO_ENABLED" to "0",
            ),
          )
          commandLine(
            goExecutable(),
            "build",
            "-trimpath",
            "-ldflags=-s -w",
            "-o",
            output.absolutePath,
            goCmdPackage,
          )
        }
      }
    platformTasks[platform.classifier] = buildTask

    tasks.register<Copy>("copyNativeToJarResources_${platform.classifier}") {
      dependsOn(buildTask)
      onlyIf { shouldBuildAllPlatforms() }
      from(nativeOutput)
      into(nativeResourceDir.map { it.dir("native/${platform.classifier}") })
    }
  }

  val universalOutput =
    nativeDir.map { it.file("${ProtocPlatforms.UNIVERSAL_CLASSIFIER}/$binaryName") }
  val universalTask =
    tasks.register<Exec>("buildNativeBinary_${ProtocPlatforms.UNIVERSAL_CLASSIFIER}") {
      group = "build"
      description = "Build fat macOS binary for $binaryName"
      dependsOn(
        platformTasks.getValue("osx-aarch_64"),
        platformTasks.getValue("osx-x86_64"),
      )
      onlyIf { shouldBuildAllPlatforms() && OperatingSystem.current().isMacOsX }
      outputs.file(universalOutput)
      doFirst {
        val output = universalOutput.get().asFile
        output.parentFile.mkdirs()
        val aarch =
          nativeDir.get().file("osx-aarch_64/$binaryName").asFile.absolutePath
        val x86 =
          nativeDir.get().file("osx-x86_64/$binaryName").asFile.absolutePath
        commandLine("lipo", "-create", "-output", output.absolutePath, aarch, x86)
      }
    }

  val buildNativeBinaries =
    tasks.register<Task>("buildNativeBinaries") {
      group = "build"
      description = "Build native binaries for $binaryName"
      dependsOn(buildHostNativeBinary)
      dependsOn(platformTasks.values)
      dependsOn(universalTask)
      ProtocPlatforms.all.forEach { platform ->
        dependsOn(tasks.named("copyNativeToJarResources_${platform.classifier}"))
      }
      outputs.dir(nativeResourceDir)
      outputs.dir(nativeDir)
    }

  val platformExecutables = linkedMapOf<String, Provider<RegularFile>>()
  if (shouldBuildAllPlatforms()) {
    ProtocPlatforms.all.forEach { platform ->
      val jarBinaryName = ProtocPlatforms.jarBinaryName(binaryName, platform)
      platformExecutables[platform.classifier] =
        nativeDir.map { it.file("${platform.classifier}/$jarBinaryName") }
    }
    if (OperatingSystem.current().isMacOsX) {
      platformExecutables[ProtocPlatforms.UNIVERSAL_CLASSIFIER] = universalOutput
    }
  } else {
    platformExecutables[hostPlatform.classifier] =
      nativeDir.map { it.file("${hostPlatform.classifier}/$hostJarBinaryName") }
  }

  return ProtocPluginNativeBuild(
    buildNativeBinaries,
    platformExecutables,
    nativeResourceDir,
  )
}
