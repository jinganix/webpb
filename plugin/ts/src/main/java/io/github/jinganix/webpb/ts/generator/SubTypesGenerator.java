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

package io.github.jinganix.webpb.ts.generator;

import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.ts.utils.Imports;
import io.github.jinganix.webpb.ts.utils.TsUtils;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import io.github.jinganix.webpb.utilities.utils.Templates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Generator for {@link Descriptor}. */
public class SubTypesGenerator {

  /** Constructor. */
  public SubTypesGenerator() {}

  /**
   * Generate sub types.
   *
   * @param descriptors descriptors
   * @return generated content
   */
  public Map<String, String> generate(List<FileDescriptor> descriptors) {
    Map<String, List<Descriptor>> extendsMap = new HashMap<>();
    for (FileDescriptor fileDescriptor : descriptors) {
      for (Descriptor descriptor : fileDescriptor.getMessageTypes()) {
        OptMessageOpts opt = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
        if (opt.getExtends().isEmpty() || opt.getSubValuesList().isEmpty()) {
          continue;
        }
        extendsMap.computeIfAbsent(opt.getExtends(), (k) -> new ArrayList<>()).add(descriptor);
      }
    }
    Map<String, String> fileMap = new HashMap<>();
    for (Entry<String, List<Descriptor>> entry : extendsMap.entrySet()) {
      String content = genContent(entry.getKey(), entry.getValue());
      fileMap.put(entry.getKey() + "Types.ts", content);
    }
    return fileMap;
  }

  private String genContent(String typeName, List<Descriptor> descriptors) {
    Imports imports = new Imports();
    Map<String, Object> data = new HashMap<>();
    List<String> types = new ArrayList<>();
    for (Descriptor descriptor : descriptors) {
      types.add(TsUtils.toInterfaceName(descriptor.getFullName()));
      imports.importType(descriptor.getFullName());
    }
    data.put("types_name", typeName + "Types");
    data.put("imports", imports.toList());
    data.put("types", types);
    return new Templates().process("file.types.ftl", data);
  }
}
