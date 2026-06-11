import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto
import com.google.protobuf.gradle.remove
import org.gradle.api.file.DuplicatesStrategy
import utils.Vers.versionProtobufJava
import utils.Vers.webpb
import utils.goProtocPluginPath
import utils.hierarchicalGroup
import java.lang.RuntimeException

plugins {
  idea
  id("java.common")
  id("com.google.protobuf")
}

group = hierarchicalGroup()

dependencies {
  implementation(project(":lib:proto"))
}

listOf("proto2", "proto3").forEach { syntax ->
  file("src/proto/$syntax").listFiles()?.filter { it.isDirectory }?.forEach { case ->
    val sourceSetName = "${syntax}_${case.name}"
    sourceSets.create(sourceSetName) {
      proto {
        srcDir(case.absolutePath)
      }
    }
  }
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${versionProtobufJava}"
  }
  plugins {
    id("dump") {
      path = goProtocPluginPath("dump")
    }
  }
  generateProtoTasks {
    sourceSets.forEach { set ->
      ofSourceSet(set.name).forEach {
        it.addIncludeDir(files(layout.buildDirectory.dir("extracted-include-protos/main").get()))
        it.builtins {
          remove("java")
        }
        it.plugins {
          id("dump")
        }
      }
    }
  }
}

val generatingTasks = tasks.filter { "generate(\\w*)Proto".toRegex().matches(it.name) }
generatingTasks.forEach {
  it.dependsOn(
    ":plugin:buildGoDump",
    ":plugin:buildGoJava",
    ":plugin:buildGoTs",
  )
  it.dependsOn(tasks.extractIncludeProto)
}

tasks.jar {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  from(layout.buildDirectory.dir("generated/sources/proto").get())
  dependsOn(generatingTasks)
}

