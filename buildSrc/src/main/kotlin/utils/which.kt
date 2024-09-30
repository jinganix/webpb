package utils

import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.which(command: String): String? {
  return try {
    val outputStream = ByteArrayOutputStream()
    exec {
      commandLine("which", command)
      isIgnoreExitValue = true
      standardOutput = outputStream
    }
    outputStream.toString().trim()
  } catch (e: Exception) {
    command
  }
}
