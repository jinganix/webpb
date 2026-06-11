plugins {
  base
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
