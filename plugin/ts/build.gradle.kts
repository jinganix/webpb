import utils.signAndPublish

plugins {
  id("java.application")
}

dependencies {
  implementation(project(":lib:commons"))
  implementation(project(":lib:utilities"))
  testImplementation(project(":lib:tests"))
}

val artifactId = "webpb-protoc-ts"

tasks.bootJar {
  archiveBaseName.set(artifactId)
}

signAndPublish(artifactId, "The webpb protoc plugin for TypeScript")
