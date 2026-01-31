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

package io.github.jinganix.webpb.java.generator;

import static com.google.protobuf.Descriptors.EnumDescriptor;
import static com.google.protobuf.Descriptors.FileDescriptor;

import com.google.protobuf.Descriptors.Descriptor;
import io.github.jinganix.webpb.java.utils.GeneratorUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Generator to process {@link Descriptor}. */
public final class Generator {

  /**
   * Create a generator.
   *
   * @return {@link Generator}
   */
  public static Generator create() {
    return new Generator();
  }

  private static String filename(String javaPackage, String className) {
    return javaPackage.replaceAll("\\.", "/") + "/" + className + ".java";
  }

  private static boolean shouldIgnore(String packageName) {
    return packageName.startsWith("com.google.protobuf")
        || packageName.startsWith("io.github.jinganix.webpb.utilities.descriptor");
  }

  /** Generate. */
  public Map<String, String> generate(FileDescriptor fd) {
    try {
      String javaPackage = GeneratorUtils.getJavaPackage(fd);
      if (shouldIgnore(javaPackage)) {
        return Collections.emptyMap();
      }
      Map<String, String> fileMap = new HashMap<>();
      for (Descriptor descriptor : fd.getMessageTypes()) {
        String content = new MessageGenerator(fd).generate(descriptor);
        fileMap.put(filename(javaPackage, descriptor.getName()), content);
      }
      for (EnumDescriptor enumDescriptor : fd.getEnumTypes()) {
        String content = new EnumGenerator(fd).generate(enumDescriptor, "enum.ftl");
        fileMap.put(filename(javaPackage, enumDescriptor.getName()), content);

        content = new EnumGenerator(fd).generate(enumDescriptor, "enum.values.ftl");
        fileMap.put(filename(javaPackage, enumDescriptor.getName() + "Values"), content);

        content = new EnumGenerator(fd).generate(enumDescriptor, "enum.names.ftl");
        fileMap.put(filename(javaPackage, enumDescriptor.getName() + "Names"), content);
      }
      return fileMap;
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse: " + fd.getName(), e);
    }
  }
}
