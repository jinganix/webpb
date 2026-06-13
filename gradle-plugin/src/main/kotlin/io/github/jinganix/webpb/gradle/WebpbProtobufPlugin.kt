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

package io.github.jinganix.webpb.gradle

import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.remove
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import java.io.File

abstract class WebpbProtobufPlugin(
  private val protocPluginId: String,
  private val protocArtifactId: String,
) : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.google.protobuf")
    val extension = project.extensions.create<WebpbExtension>("webpb")

    project.afterEvaluate {
      val webpbVersion = extension.webpbVersion ?: WebpbVersions.pluginReleaseVersion()
      val localPath = extension.localPluginPath ?: detectLocalPluginPath(project, protocArtifactId)

      project.extensions.configure<ProtobufExtension> {
        protoc {
          artifact = "com.google.protobuf:protoc:${extension.protobufVersion}"
        }
        plugins {
          clearGrpcPluginLocator(this)
          id(protocPluginId) {
            if (localPath != null) {
              path = localPath
            } else {
              artifact =
                "io.github.jinganix.webpb:$protocArtifactId:$webpbVersion"
            }
          }
        }
        generateProtoTasks {
          extension.sourceSets.forEach { sourceSetName ->
            ofSourceSet(sourceSetName).forEach { task ->
              if (extension.cleanOutput) {
                task.doFirst {
                  project.delete(task.outputBaseDir)
                }
              }
              task.builtins {
                remove("java")
              }
              task.plugins {
                clearGrpcTaskPlugin(this)
                id(protocPluginId)
              }
            }
          }
        }
      }
    }
  }

  /** Spring Boot 4.1+ auto-configures a gRPC protoc plugin when both plugins are present. */
  private fun clearGrpcPluginLocator(
    plugins: org.gradle.api.NamedDomainObjectContainer<com.google.protobuf.gradle.ExecutableLocator>,
  ) {
    plugins.findByName("grpc")?.let(plugins::remove)
  }

  private fun clearGrpcTaskPlugin(
    plugins: org.gradle.api.NamedDomainObjectContainer<GenerateProtoTask.PluginOptions>,
  ) {
    plugins.findByName("grpc")?.let(plugins::remove)
  }

  private fun detectLocalPluginPath(project: Project, protocArtifactId: String): String? {
    val binaryName =
      protocArtifactId.removePrefix("webpb-protoc-")
    val ext = if (OperatingSystem.current().isWindows) ".exe" else ""
    val candidates =
      listOf(
        project.rootProject.layout.projectDirectory
          .file("plugin/bin/webpb-protoc-$binaryName$ext")
          .asFile,
        project.layout.projectDirectory
          .file("plugin/bin/webpb-protoc-$binaryName$ext")
          .asFile,
      )
    return candidates.firstOrNull(File::exists)?.absolutePath
  }
}
