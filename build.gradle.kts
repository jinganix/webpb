plugins {
  base
}

val gradlePluginBuild = gradle.includedBuild("gradle-plugin")

tasks.register("publishToMavenLocal") {
  group = "publishing"
  description = "Publishes all Maven artifacts including gradle-plugin to the local Maven repository"
  dependsOn(gradlePluginBuild.task(":publishToMavenLocal"))
}

tasks.register("publish") {
  group = "publishing"
  description = "Publishes all Maven artifacts including gradle-plugin"
  dependsOn(gradlePluginBuild.task(":publish"))
}

subprojects {
  afterEvaluate {
    if (tasks.findByName("publishToMavenLocal") != null) {
      rootProject.tasks.named("publishToMavenLocal") {
        dependsOn(tasks.named("publishToMavenLocal"))
      }
    }
    if (tasks.findByName("publish") != null) {
      rootProject.tasks.named("publish") {
        dependsOn(tasks.named("publish"))
      }
    }
  }
}

val jacocoProjects =
  listOf(
    ":lib:commons",
    ":lib:utilities",
    ":runtime:java",
    ":runtime:processor",
    ":sample:backend",
  )

tasks.register("jacocoReport") {
  group = "verification"
  description = "Generate JaCoCo XML reports for all Java subprojects with unit tests"
  dependsOn(jacocoProjects.map { project(it).tasks.named("jacocoTestReport") })
}

tasks.register("coverageReport") {
  group = "verification"
  description = "Generate coverage reports for all subprojects with tests"
  dependsOn(
    tasks.named("jacocoReport"),
    project(":plugin").tasks.named("testGoCoverage"),
  )
}
