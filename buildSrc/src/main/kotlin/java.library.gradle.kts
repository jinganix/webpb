import utils.Vers.versionJupiter

plugins {
  `java-library`
  `maven-publish`
  id("java.common")
  id("com.vanniktech.maven.publish")
  signing
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:${versionJupiter}")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${versionJupiter}")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
