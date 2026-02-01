import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
  `kotlin-dsl`
  `maven-publish`
}

val javaVersion = JavaVersion.VERSION_21

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

tasks.compileKotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
  }
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

val properties = Properties()
FileInputStream(file("../gradle.properties")).use(properties::load)

for (key in properties.stringPropertyNames()) {
  ext.set(key, properties.getProperty(key))
}

val versionDependencyManagementPlugin: String by project
val versionFreemarker: String by project
val versionGoogleJavaFormat: String by project
val versionGradleMavenPublishPlugin: String by project
val versionGradleVersionsPlugin: String by project
val versionJacoco: String by project
val versionProtobufGradlePlugin: String by project
val versionSpotlessPluginGradle: String by project
val versionSpringBootGradlePlugin: String by project

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:${versionSpotlessPluginGradle}")
  implementation("com.github.ben-manes:gradle-versions-plugin:${versionGradleVersionsPlugin}")
  implementation("com.google.googlejavaformat:google-java-format:${versionGoogleJavaFormat}")
  implementation("com.google.protobuf:protobuf-gradle-plugin:${versionProtobufGradlePlugin}")
  implementation("com.vanniktech:gradle-maven-publish-plugin:${versionGradleMavenPublishPlugin}")
  implementation("io.spring.gradle:dependency-management-plugin:${versionDependencyManagementPlugin}")
  implementation("org.freemarker:freemarker:${versionFreemarker}")
  implementation("org.jacoco:org.jacoco.agent:${versionJacoco}")
  implementation("org.jacoco:org.jacoco.ant:${versionJacoco}")
  implementation("org.springframework.boot:spring-boot-gradle-plugin:${versionSpringBootGradlePlugin}")
  implementation(kotlin("script-runtime"))
}
