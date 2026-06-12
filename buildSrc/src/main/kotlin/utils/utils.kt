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

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import java.util.*

import org.gradle.internal.os.OperatingSystem

fun Project.goProtocPluginPath(name: String): String {
  val ext = if (OperatingSystem.current().isWindows) ".exe" else ""
  return java.io.File(rootDir, "plugin/bin/webpb-protoc-$name$ext").absolutePath
}

fun goExecutable(): String {
  val os = OperatingSystem.current()
  val goName = if (os.isWindows) "go.exe" else "go"

  System.getenv("WEBPB_GO")?.takeIf { it.isNotBlank() }?.let { configured ->
    val candidate = File(configured.trim())
    if (candidate.isFile) {
      return candidate.absolutePath
    }
  }

  System.getenv("GOROOT")?.takeIf { it.isNotBlank() }?.let { goroot ->
    val candidate = File(goroot, "bin${File.separator}$goName")
    if (candidate.isFile) {
      return candidate.absolutePath
    }
  }

  System.getenv("PATH")?.split(File.pathSeparatorChar)?.forEach { dir ->
    if (dir.isBlank()) {
      return@forEach
    }
    val candidate = File(dir, goName)
    if (candidate.isFile) {
      return candidate.absolutePath
    }
  }

  resolveGoFromShell(os, goName)?.let { return it }

  error(
    "Go executable not found. Install Go, set GOROOT, or set WEBPB_GO to the go binary path.",
  )
}

private fun resolveGoFromShell(os: OperatingSystem, goName: String): String? {
  val command =
    if (os.isWindows) {
      listOf("where.exe", goName)
    } else {
      listOf("/bin/sh", "-lc", "command -v $goName")
    }
  val process =
    ProcessBuilder(command)
      .redirectErrorStream(true)
      .apply { environment().putAll(System.getenv()) }
      .start()
  val output = process.inputStream.bufferedReader().readText().trim()
  if (process.waitFor() != 0 || output.isBlank()) {
    return null
  }
  val candidate = File(output.lineSequence().first().trim())
  return candidate.takeIf { it.isFile }?.absolutePath
}

fun Project.hierarchicalGroup(): String {
  var suffix = ""
  var proj = project.parent
  while (rootProject != proj && proj != null) {
    suffix = "." + proj.name + suffix
    proj = proj.parent!!
  }
  return project.group.toString() + suffix
}

fun Project.signAndPublish(
  artifactId: String,
  desc: String,
  platformExecutables: Map<String, Provider<RegularFile>> = emptyMap(),
  platformExecutablesBuiltBy: TaskProvider<*>? = null,
) {
  val extension = extensions.getByType<MavenPublishBaseExtension>()

  if (System.getenv("GITHUB_ACTIONS")?.toBoolean() == true) {
    extension.publishToMavenCentral(automaticRelease = true)
    extension.signAllPublications()
  }

  val publicationName = "[_-]+[a-zA-Z]".toRegex().replace(artifactId) { it ->
    it.value.replace("_", "").replace("-", "")
      .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
  }

  extension.coordinates(group.toString(), artifactId, version.toString())

  val publishing = project.the<PublishingExtension>()
  val publication = publishing.publications.create(publicationName, MavenPublication::class.java)
  publication.artifactId = artifactId
  publication.pom {
    name.set(publicationName)
    url.set("https://github.com/jinganix/webpb")
    description.set(desc)
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("gan.jin")
        name.set("JinGan")
        email.set("jinganix@gmail.com")
      }
    }
    scm {
      connection.set("scm:git:git://github.com/jinganix/webpb.git")
      developerConnection.set("scm:git:ssh://github.com/jinganix/webpb.git")
      url.set("https://github.com/jinganix/webpb")
    }
  }

  val bootJarTask = tasks.findByName("bootJar")
  if (bootJarTask is org.gradle.api.tasks.bundling.Jar) {
    publication.artifact(bootJarTask) {
      classifier = "all"
    }
  }

  platformExecutables.forEach { (classifier, executable) ->
    publication.artifact(executable) {
      this.classifier = classifier
      this.extension = "exe"
      platformExecutablesBuiltBy?.let { builtBy(it) }
    }
  }
}

fun Project.createConfiguration(
  name: String,
  docsType: String,
  configuration: Action<Configuration>
): Configuration {
  val conf = configurations.create(name) {
    isCanBeResolved = false
    attributes {
      attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
      attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
      attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(docsType))
    }
  }
  configuration.execute(conf)
  return conf
}

fun Project.extractDependencies(file: File): List<String> {
  val text = file.readText()
  val versionRegex = "(.*)\\$\\{?([\\w+]*)}?".toRegex()
  return "(implementation|testImplementation)\\(\"(.*)\"\\)".toRegex()
    .findAll(text)
    .map { it.groupValues[2] }
    .map {
      val matchResult = versionRegex.find(it) ?: return@map it
      val artifact = matchResult.groupValues[1]
      val property = matchResult.groupValues[2]
      "$artifact${project.property(property) as String}"
    }
    .toList()
}
