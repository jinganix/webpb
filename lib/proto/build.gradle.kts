import utils.signAndPublish

plugins {
  id("java.library")
}

signAndPublish("webpb-proto") {
  from(components["java"])
  pom {
    description.set("The webpb common proto library")
  }
}
