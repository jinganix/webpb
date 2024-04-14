import com.diffplug.gradle.spotless.SpotlessExtension
import java.io.FileInputStream
import java.util.*
import utils.Props
import utils.Vers
import utils.Vers.versionAssertj
import utils.Vers.versionGoogleJavaFormat
import utils.Vers.versionJacoco
import utils.Vers.versionJakartaXml
import utils.Vers.versionLombok
import utils.Vers.versionMockitoCore
import utils.Vers.versionMockitoInline
import utils.createConfiguration

plugins {
  if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) id("com.diffplug.spotless")
  id("conventions.versioning")
  idea
  jacoco
  java
}

val javaVersion = if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
  JavaVersion.VERSION_17
} else {
  JavaVersion.VERSION_1_8
}

val properties = Properties()
if (javaVersion == JavaVersion.VERSION_1_8) {
  FileInputStream(file("../../gradle.java8.properties")).use(properties::load)
}
Props.initialize(project)
Vers.initialize(project, properties)

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri(Props.snapshotRepo) }
}

dependencies {
  annotationProcessor("org.projectlombok:lombok:${versionLombok}")
  compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:${versionJakartaXml}")
  compileOnly("org.projectlombok:lombok:${versionLombok}")
  testAnnotationProcessor("org.projectlombok:lombok:${versionLombok}")
  testCompileOnly("org.projectlombok:lombok:${versionLombok}")
  testImplementation("org.assertj:assertj-core:${versionAssertj}")
  testImplementation("org.mockito:mockito-core:${versionMockitoCore}")
  testImplementation("org.mockito:mockito-inline:${versionMockitoInline}")
}

tasks.test {
  useJUnitPlatform()
}

if (javaVersion == JavaVersion.VERSION_17) {
  extensions.findByType<SpotlessExtension>()?.java {
    targetExclude("build/**/*")
    googleJavaFormat(versionGoogleJavaFormat)
  }

  tasks.check {
    dependsOn(tasks.findByName("spotlessCheck"))
  }
}

jacoco {
  toolVersion = versionJacoco
}

tasks.jacocoTestReport {
  enabled = false
}

createConfiguration("outgoingClassDirs", "classDirs") {
  extendsFrom(configurations.implementation.get())
  isCanBeResolved = false
  isCanBeConsumed = true
  sourceSets.main.get().output.forEach {
    outgoing.artifact(it)
  }
}

createConfiguration("outgoingSourceDirs", "sourceDirs") {
  extendsFrom(configurations.implementation.get())
  isCanBeResolved = false
  isCanBeConsumed = true
  sourceSets.main.get().java.srcDirs.forEach {
    outgoing.artifact(it)
  }
}

createConfiguration("outgoingCoverageData", "coverageData") {
  extendsFrom(configurations.implementation.get())
  isCanBeResolved = false
  isCanBeConsumed = true
  outgoing.artifact(tasks.test.map {
    it.extensions.getByType<JacocoTaskExtension>().destinationFile!!
  })
}
