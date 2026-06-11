import utils.Vers.versionCompileTesting
import utils.Vers.versionSpringFramework
import utils.signAndPublish

plugins {
  id("java.library")
}

val javacInternalsExports =
  listOf(
    "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
  )

val javacInternalsOpens =
  listOf(
    "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
  )

if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
  tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(javacInternalsExports)
  }
}

tasks.test {
  jvmArgs(javacInternalsOpens)
}

dependencies {
  testImplementation("com.google.testing.compile:compile-testing:${versionCompileTesting}")
  testImplementation("org.springframework:spring-messaging:${versionSpringFramework}")
  testImplementation("org.springframework:spring-web:${versionSpringFramework}")
  testImplementation(project(":runtime:java"))
}

signAndPublish("webpb-processor", "The webpb annotation processor for JAVA")

val jacocoClassDirs =
  sourceSets.main.get().output.asFileTree.matching {
    exclude("io/github/jinganix/webpb/processor/misc/*")
  }
tasks.jacocoTestReport {
  classDirectories.setFrom(jacocoClassDirs)
}
tasks.jacocoTestCoverageVerification {
  classDirectories.setFrom(jacocoClassDirs)
}

tasks.javadoc {
  enabled = false
}
