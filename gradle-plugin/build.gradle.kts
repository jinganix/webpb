import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val gradleProperties = Properties()
file("../gradle.properties").inputStream().use { gradleProperties.load(it) }

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  id("com.vanniktech.maven.publish")
  signing
}

val javaVersion = JavaVersion.VERSION_21

group = gradleProperties.getProperty("group")
version = gradleProperties.getProperty("version")

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

val versionProtobufGradlePlugin: String =
  gradleProperties.getProperty("versionProtobufGradlePlugin")

dependencies {
  implementation("com.google.protobuf:protobuf-gradle-plugin:$versionProtobufGradlePlugin")
}

gradlePlugin {
  plugins {
    create("webpbJava") {
      id = "io.github.jinganix.webpb.java"
      implementationClass = "io.github.jinganix.webpb.gradle.WebpbJavaPlugin"
      displayName = "webpb Java protobuf plugin"
      description = "Configures protobuf code generation with the webpb Java protoc plugin"
    }
    create("webpbTs") {
      id = "io.github.jinganix.webpb.ts"
      implementationClass = "io.github.jinganix.webpb.gradle.WebpbTsPlugin"
      displayName = "webpb TypeScript protobuf plugin"
      description = "Configures protobuf code generation with the webpb TypeScript protoc plugin"
    }
  }
}

val versionProtobufJava: String = gradleProperties.getProperty("versionProtobufJava")

tasks.processResources {
  inputs.property("webpbVersion", version)
  inputs.property("protobufVersion", versionProtobufJava)
  filesMatching("webpb-version.properties") {
    expand(
      "webpbVersion" to version,
      "protobufVersion" to versionProtobufJava,
    )
  }
}

if (System.getenv("GITHUB_ACTIONS")?.toBoolean() == true) {
  extensions.configure<MavenPublishBaseExtension> {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
  }
}

extensions.configure<MavenPublishBaseExtension> {
  coordinates(group.toString(), "webpb-gradle-plugin", version.toString())
  pom {
    name.set("webpb-gradle-plugin")
    description.set("Gradle convention plugins for webpb protobuf code generation")
    url.set("https://github.com/jinganix/webpb")
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
}
