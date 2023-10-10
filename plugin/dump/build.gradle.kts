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
  launchScript()
}

signAndPublish(artifactId) {
  artifact(tasks.bootJar.get()) { classifier = "all" }
  pom {
    description.set("The webpb protoc plugin to dump generator request")
  }
}
