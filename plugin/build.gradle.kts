import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.internal.os.OperatingSystem
import utils.goExecutable

plugins {
  base
  id("com.diffplug.spotless")
}

val versionGoogleJavaFormat: String by project

repositories {
  mavenCentral()
}

extensions.configure<SpotlessExtension> {
  java {
    target("testdata/java/**/*.java", "build/golden-format/java/**/*.java")
    googleJavaFormat(versionGoogleJavaFormat)
  }
}

tasks.register<Exec>("formatTestdataTs") {
  group = "formatting"
  description = "Format plugin/testdata/ts with project prettier and eslint"
  workingDir = layout.projectDirectory.asFile
  val eslint = rootProject.layout.projectDirectory.file("runtime/ts/node_modules/.bin/eslint")
  val prettier = rootProject.layout.projectDirectory.file("runtime/ts/node_modules/.bin/prettier")
  val prettierConfig = rootProject.layout.projectDirectory.file("runtime/ts/.prettierrc.js")
  commandLine(
    "bash",
    "-c",
    """
    set -euo pipefail
    if [ ! -x '${prettier.asFile.path}' ]; then
      echo 'Run npm ci in runtime/ts first' >&2
      exit 1
    fi
    '${prettier.asFile.path}' --write 'testdata/ts/**/*.ts' --ignore-path .prettierignore --config '${prettierConfig.asFile.path}'
    if [ -x '${eslint.asFile.path}' ]; then
      '${eslint.asFile.path}' --fix 'testdata/ts/**/*.ts'
    fi
    """.trimIndent(),
  )
}

tasks.register("formatTestdata") {
  group = "formatting"
  description = "Format plugin/testdata with spotless (Java) and prettier/eslint (TS)"
  dependsOn(tasks.named("spotlessApply"), tasks.named("formatTestdataTs"))
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

fun configureGoTestDeps(task: Task) {
  task.dependsOn(buildGoDump, buildGoJava, buildGoTs)
  task.dependsOn(project(":lib:tests").tasks.named("jar"))
}

tasks.register<Exec>("testGo") {
  group = "verification"
  description = "Run Go plugin tests (Java and TS golden)"
  onlyIf { !skipGoTest.get() }
  configureGoTestDeps(this)
  workingDir = projectDir
  doFirst {
    environment(System.getenv())
    commandLine(goExecutable(), "test", "./...")
  }
}

val goCoverageProfile = layout.buildDirectory.file("reports/coverage/coverage.out")

tasks.register<Exec>("testGoCoverage") {
  group = "verification"
  description = "Run Go plugin tests and write a coverage profile for Codecov"
  onlyIf { !skipGoTest.get() }
  configureGoTestDeps(this)
  workingDir = projectDir
  outputs.file(goCoverageProfile)
  doFirst {
    val profile = goCoverageProfile.get().asFile
    profile.parentFile.mkdirs()
    environment(System.getenv())
    commandLine(
      goExecutable(),
      "test",
      "-coverprofile=${profile.absolutePath}",
      "-covermode=atomic",
      "-coverpkg=./...",
      "./...",
    )
  }
}

tasks.register<Exec>("verifyGoCoverage") {
  group = "verification"
  description = "Verify each Go source file has at least 90% line coverage"
  onlyIf { !skipGoTest.get() }
  dependsOn(tasks.named("testGoCoverage"))
  val script = rootProject.layout.projectDirectory.file("scripts/check-go-file-coverage.py")
  val profile = goCoverageProfile
  doFirst {
    commandLine("python3", script.asFile.absolutePath, profile.get().asFile.absolutePath, "--min", "90")
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
  dependsOn(tasks.named("testGo"), tasks.named("verifyGoCoverage"))
}
