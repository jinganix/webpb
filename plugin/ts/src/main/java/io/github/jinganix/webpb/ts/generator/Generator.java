/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/jinganix/webpb
 */

package io.github.jinganix.webpb.ts.generator;

import static com.google.protobuf.Descriptors.EnumDescriptor;
import static com.google.protobuf.Descriptors.FileDescriptor;
import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;
import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getWebpbOpts;

import com.google.protobuf.Descriptors.Descriptor;
import io.github.jinganix.webpb.ts.utils.Imports;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend;
import io.github.jinganix.webpb.utilities.utils.DescriptorUtils;
import io.github.jinganix.webpb.utilities.utils.Templates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/** Generator to process {@link Descriptor}. */
public final class Generator {

  private final Templates templates = new Templates();

  /**
   * Create a generator.
   *
   * @return {@link Generator}
   */
  public static Generator create() {
    return new Generator();
  }

  private static boolean shouldIgnore(String packageName) {
    return StringUtils.isEmpty(packageName) || packageName.contains("google.protobuf");
  }

  private static List<String> getImports(FileDescriptor fd) {
    return Stream.of(
            getWebpbOpts(fd, WebpbExtend.FileOpts::hasTs).getTs().getImportList(),
            getOpts(fd, WebpbExtend.FileOpts::hasTs).getTs().getImportList())
        .flatMap(List::stream)
        .distinct()
        .sorted(StringUtils::compare)
        .collect(Collectors.toList());
  }

  private static List<String> getLookup(FileDescriptor fd) {
    return Stream.of(
            DescriptorUtils.resolveTopLevelTypes(fd).stream()
                .map(DescriptorUtils::resolveNestedTypes)
                .flatMap(List::stream)
                .map(e -> "./" + e.getFullName().replace(".", "/"))
                .collect(Collectors.toList()))
        .flatMap(List::stream)
        .filter(e -> !e.contains("google"))
        .distinct()
        .sorted(StringUtils::compare)
        .collect(Collectors.toList());
  }

  /**
   * Entrance of the generator.
   *
   * @param fd {@link FileDescriptor}
   * @return generated content
   */
  public String generate(FileDescriptor fd) {
    if (shouldIgnore(fd.getPackage())) {
      return null;
    }
    Imports imports = new Imports(fd.getPackage(), getImports(fd), getLookup(fd));
    List<String> messages = new ArrayList<>();
    for (Descriptor descriptor : fd.getMessageTypes()) {
      messages.add(new MessageGenerator(imports, fd).generate(descriptor));
    }
    for (EnumDescriptor enumDescriptor : fd.getEnumTypes()) {
      messages.add(new EnumGenerator(fd).generate(enumDescriptor));
    }
    Map<String, Object> data = new HashMap<>();
    data.put("filename", fd.getName());
    data.put("imports", imports.toList());
    data.put("messages", messages);
    return templates.process("file.ftl", data);
  }
}
