import utils.Vers.versionCommonsIo
import utils.Vers.versionCommonsLang3
import utils.Vers.versionFreemarker
import utils.Vers.versionLombok
import utils.Vers.versionProtobufJava

plugins {
  id("com.google.protobuf")
  id("java.library")
}

dependencies {
  annotationProcessor("org.projectlombok:lombok:${versionLombok}")
  api("org.freemarker:freemarker:${versionFreemarker}")
  compileOnly("org.projectlombok:lombok:${versionLombok}")
  compileOnly(project(":lib:tests"))
  implementation("com.google.protobuf:protobuf-java:${versionProtobufJava}")
  implementation("commons-io:commons-io:${versionCommonsIo}")
  implementation("org.apache.commons:commons-lang3:${versionCommonsLang3}")
  implementation(project(":lib:commons"))
  implementation(project(":lib:proto"))
  testAnnotationProcessor("org.projectlombok:lombok:${versionLombok}")
  testCompileOnly("org.projectlombok:lombok:${versionLombok}")
  testImplementation(project(":lib:tests"))
}

val extractSources by tasks.registering(DefaultTask::class) {
  sourceSets {
    main {
      proto {
        srcDir(layout.buildDirectory.dir("extracted-include-protos/main/webpb").get())
      }
    }
  }
  dependsOn("extractIncludeProto")
}

tasks.processResources {
  dependsOn(extractSources)
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${versionProtobufJava}"
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.dependsOn(extractSources)
    }
  }
}

tasks.javadoc {
  exclude("io/github/jinganix/webpb/utilities/descriptor/**")
}
