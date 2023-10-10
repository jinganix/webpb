import utils.Vers.versionCommonsLang3
import utils.Vers.versionJupiter
import utils.Vers.versionProtobufJava

plugins {
  java
  `maven-publish`
  application
  id("java.common")
  id("org.springframework.boot")
  signing
}

dependencies {
  implementation("com.google.protobuf:protobuf-java:${versionProtobufJava}")
  implementation("org.apache.commons:commons-lang3:${versionCommonsLang3}")
  testImplementation("org.junit.jupiter:junit-jupiter-api:${versionJupiter}")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${versionJupiter}")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
