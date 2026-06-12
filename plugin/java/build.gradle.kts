import utils.configureProtocPluginNative
import utils.signAndPublish

plugins {
  id("java.application")
}

application {
  mainClass.set("io.github.jinganix.webpb.java.Main")
}

val native =
  configureProtocPluginNative(
    goBuildTaskPath = ":plugin:buildGoJava",
    goCmdPackage = "./cmd/webpb-java",
    binaryName = "webpb-protoc-java",
  )

val artifactId = "webpb-protoc-java"

tasks.bootJar {
  archiveBaseName.set(artifactId)
  dependsOn(native.buildNativeBinaries)
  from(native.nativeResourceDir) {
    into("")
  }
}

tasks.named("publish") {
  dependsOn(native.buildNativeBinaries)
}

signAndPublish(
  artifactId,
  "The webpb protoc plugin for JAVA",
  native.platformExecutables,
  native.buildNativeBinaries,
)
