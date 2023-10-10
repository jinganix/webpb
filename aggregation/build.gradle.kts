import utils.createConfiguration
import utils.extractDependencies

plugins {
  id("jacoco.aggregation")
}

repositories {
  gradlePluginPortal()
}

val buildSrcDependencies = extractDependencies(file("${rootDir}/buildSrc/build.gradle.kts"))

dependencies {
  implementation(project(":lib:commons"))
  implementation(project(":lib:proto"))
  implementation(project(":lib:utilities"))
  implementation(project(":plugin:dump"))
  implementation(project(":plugin:java"))
  implementation(project(":plugin:ts"))
  implementation(project(":runtime:java"))
  implementation(project(":runtime:processor"))
  implementation(project(":sample:backend"))
  implementation(project(":sample:proto"))

  buildSrcDependencies.forEach {
    testCompileOnly(it)
  }
}

configurations.implementation.get().dependencies.forEach {
  if (it is ModuleDependency) {
    it.isTransitive = false
  }
}

val incomingClassDirs = createConfiguration("incomingClassDirs", "classDirs") {
  extendsFrom(configurations.implementation.get())
  isCanBeResolved = true
  isCanBeConsumed = false
}

val incomingSourceDirs = createConfiguration("incomingSourceDirs", "sourceDirs") {
  extendsFrom(configurations.implementation.get())
  isCanBeResolved = true
  isCanBeConsumed = false
}

val incomingCoverageData = createConfiguration("incomingCoverageData", "coverageData") {
  extendsFrom(configurations.implementation.get())
  isCanBeResolved = true
  isCanBeConsumed = false
}

fun generateJacocoReport(base: JacocoReportBase) {
  base.additionalClassDirs(incomingClassDirs.incoming.artifactView {
    lenient(true)
  }.files.asFileTree.matching {
    exclude("io/github/jinganix/webpb/processor/misc/*")
    exclude("io/github/jinganix/webpb/runtime/mvc/VariablesResolver")
    exclude("io/github/jinganix/webpb/utilities/descriptor/*")
  })
  base.additionalSourceDirs(incomingSourceDirs.incoming.artifactView { lenient(true) }.files)
  base.executionData(incomingCoverageData.incoming.artifactView { lenient(true) }.files.filter { it.exists() })
}

val coverageVerification by tasks.registering(JacocoCoverageVerification::class) {
  group = "verification"
  generateJacocoReport(this)

  violationRules {
    rule { limit { minimum = BigDecimal.valueOf(utils.Props.jacocoMinCoverage) } }
  }
}

val coverageReport by tasks.registering(JacocoReport::class) {
  group = "verification"
  generateJacocoReport(this)

  reports {
    csv.required.set(false)
    html.required.set(true)
    xml.required.set(false)
  }
}

val configCoveralls by tasks.registering(DefaultTask::class) {
  coveralls {
    sourceDirs = incomingSourceDirs.incoming.artifactView { lenient(true) }.files.map {
      it.absolutePath
    }
    jacocoReportPath = "build/reports/jacoco/coverage/coverage.xml"
  }
}

tasks.coveralls {
  dependsOn(configCoveralls)
}

tasks.check {
  dependsOn(coverageVerification)
}
