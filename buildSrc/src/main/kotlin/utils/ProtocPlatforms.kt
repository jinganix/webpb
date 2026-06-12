/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/jinganix/webpb
 */

package utils

import org.gradle.internal.os.OperatingSystem

/** A protoc/osdetector platform with Go cross-compilation coordinates. */
data class ProtocPlatform(
  val classifier: String,
  val goOs: String,
  val goArch: String,
)

/** Platforms published for {@code com.google.protobuf:protoc} on Maven Central. */
object ProtocPlatforms {
  val all: List<ProtocPlatform> =
    listOf(
      ProtocPlatform("linux-aarch_64", "linux", "arm64"),
      ProtocPlatform("linux-ppcle_64", "linux", "ppc64le"),
      ProtocPlatform("linux-s390_64", "linux", "s390x"),
      ProtocPlatform("linux-x86_32", "linux", "386"),
      ProtocPlatform("linux-x86_64", "linux", "amd64"),
      ProtocPlatform("osx-aarch_64", "darwin", "arm64"),
      ProtocPlatform("osx-x86_64", "darwin", "amd64"),
      ProtocPlatform("windows-x86_32", "windows", "386"),
      ProtocPlatform("windows-x86_64", "windows", "amd64"),
    )

  const val UNIVERSAL_CLASSIFIER: String = "osx-universal_binary"

  fun hostPlatform(): ProtocPlatform {
    val os = OperatingSystem.current()
    val arch = System.getProperty("os.arch").lowercase()
    return when {
      os.isMacOsX && arch in setOf("aarch64", "arm64") ->
        all.first { it.classifier == "osx-aarch_64" }
      os.isMacOsX -> all.first { it.classifier == "osx-x86_64" }
      os.isWindows && arch in setOf("x86", "i386", "i686", "386") ->
        all.first { it.classifier == "windows-x86_32" }
      os.isWindows -> all.first { it.classifier == "windows-x86_64" }
      arch in setOf("aarch64", "arm64") ->
        all.first { it.classifier == "linux-aarch_64" }
      arch == "ppc64le" -> all.first { it.classifier == "linux-ppcle_64" }
      arch == "s390x" -> all.first { it.classifier == "linux-s390_64" }
      arch in setOf("x86", "i386", "i686", "386") ->
        all.first { it.classifier == "linux-x86_32" }
      else -> all.first { it.classifier == "linux-x86_64" }
    }
  }

  fun jarBinaryName(binaryName: String, platform: ProtocPlatform): String {
    return if (platform.goOs == "windows") {
      "$binaryName.exe"
    } else {
      binaryName
    }
  }
}
