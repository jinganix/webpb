import org.gradle.internal.os.OperatingSystem
import utils.goExecutable

plugins {
  base
}

val pluginBinDir = layout.projectDirectory.dir("bin")
val pluginExt = if (OperatingSystem.current().isWindows) ".exe" else ""

fun registerGoBuild(name: String, packagePath: String) =
  tasks.register<Exec>("buildGo${name.replaceFirstChar { it.uppercase() }}") {
    group = "build"
    description = "Build webpb-protoc-$name"
    workingDir = projectDir
    val output = pluginBinDir.file("webpb-protoc-$name$pluginExt")
    inputs.dir(layout.projectDirectory.dir("cmd/webpb-$name"))
    inputs.dir(layout.projectDirectory.dir("internal"))
    inputs.file(layout.projectDirectory.file("go.mod"))
    inputs.file(layout.projectDirectory.file("go.sum"))
    outputs.file(output)
    doFirst {
      environment(System.getenv())
      commandLine(
        goExecutable(),
        "build",
        "-o",
        output.asFile.absolutePath,
        packagePath,
      )
    }
  }

val skipGoTest =
  providers.gradleProperty("skipGoTest").map { it.equals("true", ignoreCase = true) }.orElse(false)

val buildGoDump = registerGoBuild("dump", "./cmd/webpb-dump")
val buildGoJava = registerGoBuild("java", "./cmd/webpb-java")
val buildGoTs = registerGoBuild("ts", "./cmd/webpb-ts")

tasks.named("build") {
  dependsOn(buildGoDump, buildGoJava, buildGoTs)
}

tasks.register<Exec>("testGo") {
  group = "verification"
  description = "Run Go plugin tests (Java and TS golden)"
  onlyIf { !skipGoTest.get() }
  dependsOn(
    buildGoDump,
    buildGoJava,
    buildGoTs,
    project(":lib:tests").tasks.named("classes"),
  )
  workingDir = projectDir
  doFirst {
    environment(System.getenv())
    commandLine(goExecutable(), "test", "./...")
  }
}

tasks.register<Exec>("testTsGo") {
  group = "verification"
  description = "Run Go TS golden tests"
  onlyIf { !skipGoTest.get() }
  dependsOn(
    buildGoDump,
    buildGoJava,
    buildGoTs,
    project(":lib:tests").tasks.named("classes"),
  )
  workingDir = projectDir
  doFirst {
    environment(System.getenv())
    commandLine(goExecutable(), "test", "-run", "TestTSGolden", "./...")
  }
}

tasks.named("check") {
  dependsOn(tasks.named("testGo"))
}
