import utils.hierarchicalGroup

plugins {
  id("java.common")
}

group = hierarchicalGroup()

dependencies {
  implementation(project(":lib:proto"))
}

generateSourceTask(
  projectDir.resolve("src/templates/WebpbOptions.proto.ftl"),
  projectDir.resolve("build/resources/main/WebpbOptions.proto"),
  mapOf("java17" to JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17))
)
tasks.processResources { finalizedBy("generateSource") }
