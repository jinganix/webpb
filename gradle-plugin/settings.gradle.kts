pluginManagement {
  val gradleProperties = java.util.Properties()
  file("../gradle.properties").inputStream().use { gradleProperties.load(it) }

  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  plugins {
    id("com.vanniktech.maven.publish") version
      gradleProperties.getProperty("versionGradleMavenPublishPlugin")
  }
}

rootProject.name = "gradle-plugin"
