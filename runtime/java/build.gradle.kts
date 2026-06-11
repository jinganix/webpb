import utils.Vers.versionJackson
import utils.Vers.versionJakartaServletApi
import utils.Vers.versionReactorNetty
import utils.Vers.versionSpringFramework
import utils.signAndPublish

plugins {
  id("java.library")
}

dependencies {
  api(project(":lib:commons"))
  compileOnly("tools.jackson.dataformat:jackson-dataformat-xml:${versionJackson}")
  compileOnly("jakarta.servlet:jakarta.servlet-api:${versionJakartaServletApi}")
  compileOnly("org.springframework:spring-messaging:${versionSpringFramework}")
  compileOnly("org.springframework:spring-webflux:${versionSpringFramework}")
  compileOnly("org.springframework:spring-webmvc:${versionSpringFramework}")
  implementation("tools.jackson.core:jackson-databind:${versionJackson}")
  testImplementation("tools.jackson.dataformat:jackson-dataformat-xml:${versionJackson}")
  testImplementation("io.projectreactor.netty:reactor-netty:${versionReactorNetty}")
  testImplementation("jakarta.servlet:jakarta.servlet-api:${versionJakartaServletApi}")
  testImplementation("org.springframework:spring-test:${versionSpringFramework}")
  testImplementation("org.springframework:spring-webflux:${versionSpringFramework}")
  testImplementation("org.springframework:spring-webmvc:${versionSpringFramework}")
}

signAndPublish("webpb-runtime", "The webpb runtime library for JAVA")
