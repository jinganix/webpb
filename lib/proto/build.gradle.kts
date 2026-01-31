import utils.signAndPublish

plugins {
  id("java.library")
}

signAndPublish("webpb-proto", "The webpb common proto library")
