import utils.signAndPublish

plugins {
  id("java.application")
}

dependencies {
  implementation(project(":lib:commons"))
}

val artifactId = "webpb-protoc-dump"

tasks.bootJar {
  archiveBaseName.set(artifactId)
}

signAndPublish(artifactId, "The webpb protoc plugin to dump generator request")
