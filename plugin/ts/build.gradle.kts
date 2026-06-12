import utils.configureProtocPluginNative
import utils.signAndPublish

plugins {
  id("java.application")
}

val native =
  configureProtocPluginNative(
    goBuildTaskPath = ":plugin:buildGoTs",
    goCmdPackage = "./cmd/webpb-ts",
    binaryName = "webpb-protoc-ts",
  )

val artifactId = "webpb-protoc-ts"

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
  "The webpb protoc plugin for TS",
  native.platformExecutables,
  native.buildNativeBinaries,
)
