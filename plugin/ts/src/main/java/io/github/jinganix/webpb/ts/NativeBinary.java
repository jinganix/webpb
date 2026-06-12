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

package io.github.jinganix.webpb.ts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Locale;
import java.util.Set;

/** Extracts the platform-specific native protoc plugin from jar resources. */
final class NativeBinary {

  private NativeBinary() {}

  static Path extract() throws IOException {
    String platform = platformDir();
    String binaryName = isWindows() ? "webpb-protoc-ts.exe" : "webpb-protoc-ts";
    String resourcePath = "native/" + platform + "/" + binaryName;
    try (InputStream input =
        NativeBinary.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (input == null) {
        throw new IOException("Native binary not found: " + resourcePath);
      }
      Path directory = Files.createTempDirectory("webpb-protoc-ts-");
      Path binary = directory.resolve(binaryName);
      Files.copy(input, binary);
      if (!isWindows()) {
        Files.setPosixFilePermissions(
            binary,
            Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE));
      }
      binary.toFile().deleteOnExit();
      directory.toFile().deleteOnExit();
      return binary;
    }
  }

  private static String platformDir() throws IOException {
    String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
    if (os.contains("mac") || os.contains("darwin")) {
      if (arch.equals("aarch64") || arch.equals("arm64")) {
        return "osx-aarch_64";
      }
      return "osx-x86_64";
    }
    if (os.contains("linux")) {
      if (arch.equals("aarch64") || arch.equals("arm64")) {
        return "linux-aarch_64";
      }
      if (arch.equals("ppc64le")) {
        return "linux-ppcle_64";
      }
      if (arch.equals("s390x")) {
        return "linux-s390_64";
      }
      if (arch.equals("x86") || arch.equals("i386") || arch.equals("i686") || arch.equals("386")) {
        return "linux-x86_32";
      }
      return "linux-x86_64";
    }
    if (os.contains("win")) {
      if (arch.equals("x86") || arch.equals("i386") || arch.equals("i686") || arch.equals("386")) {
        return "windows-x86_32";
      }
      return "windows-x86_64";
    }
    throw new IOException("Unsupported platform: " + os + " " + arch);
  }

  private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
  }
}
