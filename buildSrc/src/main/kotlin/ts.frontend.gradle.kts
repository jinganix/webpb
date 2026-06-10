import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.remove
import utils.Props
import utils.Vers
import utils.Vers.versionProtobufJava
import utils.goProtocPluginPath
import java.util.*

plugins {
  id("com.google.protobuf")
  id("conventions.versioning")
  java
}

Props.initialize(project)
Vers.initialize(project, Properties())

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri(Props.snapshotRepo) }
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${versionProtobufJava}"
  }
  plugins {
    id("ts") {
      path = goProtocPluginPath("ts")
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.builtins {
        remove("java")
      }
      it.plugins {
        id("ts")
      }
    }
  }
}

tasks.withType<GenerateProtoTask> {
  dependsOn(":plugin:build")
}
