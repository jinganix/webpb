import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.remove
import utils.Vers.versionJacksonAnnotations
import utils.Vers.versionLombok
import utils.Vers.versionNetty
import utils.Vers.versionProtobufJava
import utils.Vers.webpb
import utils.hierarchicalGroup

plugins {
  id("com.google.protobuf")
  id("io.spring.dependency-management")
  id("java.common")
  id("org.springframework.boot")
  idea
}

group = hierarchicalGroup()

dependencies {
  annotationProcessor("org.projectlombok:lombok:${versionLombok}")
  annotationProcessor(project(":runtime:processor"))
  compileOnly("org.projectlombok:lombok:${versionLombok}")
  implementation("com.fasterxml.jackson.core:jackson-annotations:${versionJacksonAnnotations}")
  implementation("io.netty:netty-resolver-dns-native-macos:${versionNetty}:osx-aarch_64")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation(project(":runtime:java"))
  protobuf(project(":sample:proto"))
  testAnnotationProcessor("org.projectlombok:lombok:${versionLombok}")
  testAnnotationProcessor(project(":runtime:processor"))
  testCompileOnly("org.projectlombok:lombok:${versionLombok}")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-web")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${versionProtobufJava}"
  }
  plugins {
    id("webpb") {
      path = "${rootDir}/plugin/java/build/libs/webpb-protoc-java-${webpb}.jar"
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.builtins {
        remove("java")
      }
      it.plugins {
        id("webpb")
      }
    }
  }
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  enabled = false
}

tasks.generateProto {
  dependsOn(":plugin:java:build")
}

val generatedMain = layout.buildDirectory.dir("generated/sources/src/main").get()
sourceSets {
  main {
    java {
      srcDir(generatedMain)
    }
  }
  test {
    java {
      srcDir(generatedMain)
    }
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
}
