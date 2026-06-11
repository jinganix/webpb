import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.internal.os.OperatingSystem
import utils.goExecutable
import utils.signAndPublish

plugins {
  id("java.application")
}

val platforms =
  listOf(
    Triple("darwin", "arm64", "darwin-arm64"),
    Triple("darwin", "amd64", "darwin-amd64"),
    Triple("linux", "amd64", "linux-amd64"),
    Triple("windows", "amd64", "windows-amd64"),
  )

val buildAllNativePlatforms =
  providers
    .gradleProperty("buildAllNativePlatforms")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)

fun hostPlatform(): Triple<String, String, String> {
  val os = OperatingSystem.current()
  return when {
    os.isMacOsX && System.getProperty("os.arch") in setOf("aarch64", "arm64") ->
      platforms[0]
    os.isMacOsX -> platforms[1]
    os.isWindows -> platforms[3]
    else -> platforms[2]
  }
}

val nativeResourceDir = layout.buildDirectory.dir("generated/native-resources")
val pluginDir = rootProject.layout.projectDirectory.dir("plugin")
val pluginExt = if (OperatingSystem.current().isWindows) ".exe" else ""

val buildHostNativeBinary by tasks.registering(Copy::class) {
  val platform = hostPlatform().third
  dependsOn(":plugin:buildGoJava")
  from(pluginDir.file("bin/webpb-protoc-java$pluginExt"))
  into(nativeResourceDir.map { it.dir("native/$platform") })
}

platforms.forEach { (goOs, goArch, resourceName) ->
  tasks.register<Exec>("buildNativeBinary_$resourceName") {
    group = "build"
    description = "Cross-compile webpb-protoc-java for $resourceName"
    onlyIf {
      buildAllNativePlatforms.get() ||
        providers.environmentVariable("GITHUB_ACTIONS").getOrElse("false") == "true"
    }
    val ext = if (goOs == "windows") ".exe" else ""
    val output =
      nativeResourceDir.map {
        it.file("native/$resourceName/webpb-protoc-java$ext").asFile
      }
    outputs.file(output)
    doFirst {
      output.get().parentFile.mkdirs()
      environment("GOOS", goOs)
      environment("GOARCH", goArch)
      commandLine(
        goExecutable(),
        "build",
        "-o",
        output.get().absolutePath,
        "./cmd/webpb-java",
      )
    }
    workingDir = pluginDir.asFile
  }
}

val buildNativeBinaries by tasks.registering {
  dependsOn(buildHostNativeBinary)
  dependsOn(
    platforms.map { (tasks.named("buildNativeBinary_${it.third}")) },
  )
  outputs.dir(nativeResourceDir)
}

val artifactId = "webpb-protoc-java"

tasks.bootJar {
  archiveBaseName.set(artifactId)
  dependsOn(buildNativeBinaries)
  from(nativeResourceDir) {
    into("")
  }
}

tasks.named("publish") {
  dependsOn(buildNativeBinaries)
}

signAndPublish(artifactId, "The webpb protoc plugin for JAVA")
