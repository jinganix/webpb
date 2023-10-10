import utils.signAndPublish

plugins {
  id("java.library")
}

signAndPublish("webpb-${project.name}") {
  from(components["java"])
  pom {
    description.set("The webpb commons library")
  }
}
