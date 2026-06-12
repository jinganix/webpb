pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
  includeBuild("gradle-plugin")
}

rootProject.name = "webpb"
include(":lib:commons")
include(":lib:proto")
include(":lib:tests")
include(":lib:utilities")
include(":plugin")
include(":plugin:java")
include(":plugin:ts")
include(":runtime:java")
include(":runtime:processor")
include(":sample:proto")
include(":sample:backend")
