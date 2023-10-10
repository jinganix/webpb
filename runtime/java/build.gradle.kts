import utils.Vers.versionJackson
import utils.Vers.versionJakartaServletApi
import utils.Vers.versionJavaxServletApi
import utils.Vers.versionReactorNetty
import utils.Vers.versionSpringFramework
import utils.signAndPublish

plugins {
  id("java.library")
}

dependencies {
  api(project(":lib:commons"))
  compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${versionJackson}")
  compileOnly("jakarta.servlet:jakarta.servlet-api:${versionJakartaServletApi}")
  compileOnly("javax.servlet:javax.servlet-api:${versionJavaxServletApi}")
  compileOnly("org.springframework:spring-messaging:${versionSpringFramework}")
  compileOnly("org.springframework:spring-webflux:${versionSpringFramework}")
  compileOnly("org.springframework:spring-webmvc:${versionSpringFramework}")
  implementation("com.fasterxml.jackson.core:jackson-databind:${versionJackson}")
  testImplementation("io.projectreactor.netty:reactor-netty:${versionReactorNetty}")
  testImplementation("javax.servlet:javax.servlet-api:${versionJavaxServletApi}") {
    when {
      JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17) ->
        testImplementation("jakarta.servlet:jakarta.servlet-api:${versionJakartaServletApi}")
    }
  }
  testImplementation("org.springframework:spring-test:${versionSpringFramework}")
  testImplementation("org.springframework:spring-webflux:${versionSpringFramework}")
  testImplementation("org.springframework:spring-webmvc:${versionSpringFramework}")
}

signAndPublish("webpb-${project.name}") {
  from(components["java"])
  pom {
    description.set("The webpb runtime library for JAVA")
  }
}
