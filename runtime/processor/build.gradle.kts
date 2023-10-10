import utils.Vers.versionCompileTesting
import utils.Vers.versionSpringFramework
import utils.signAndPublish

plugins {
  id("java.library")
}

if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
  tasks.withType<JavaCompile> {
    val args = options.compilerArgs
    args.addAll(listOf("--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))
    args.addAll(
      listOf(
        "--add-exports",
        "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED"
      )
    )
    args.addAll(listOf("--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED"))
    args.addAll(listOf("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"))
  }
}

dependencies {
  compileOnly(files(org.gradle.internal.jvm.Jvm.current().toolsJar))
  testImplementation("com.google.testing.compile:compile-testing:${versionCompileTesting}")
  testImplementation("org.springframework:spring-messaging:${versionSpringFramework}")
  testImplementation("org.springframework:spring-web:${versionSpringFramework}")
  testImplementation(files(org.gradle.internal.jvm.Jvm.current().toolsJar))
  testImplementation(project(":runtime:java"))
}

signAndPublish("webpb-processor") {
  from(components["java"])
  pom {
    description.set("The webpb annotation processor for JAVA")
  }
}

tasks.javadoc {
  enabled = false
}
