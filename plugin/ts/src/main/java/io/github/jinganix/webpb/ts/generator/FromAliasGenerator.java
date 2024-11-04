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

import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.ts.utils.Imports;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import io.github.jinganix.webpb.utilities.utils.DescriptorUtils;
import io.github.jinganix.webpb.utilities.utils.Templates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/** Generator for {@link Descriptor}. */
public class FromAliasGenerator {

  private final Map<String, Descriptor> baseTypes = new HashMap<>();

  public Map<String, String> generate(List<FileDescriptor> descriptors) {
    Map<String, List<Descriptor>> subTypes = new HashMap<>();
    for (FileDescriptor fileDescriptor : descriptors) {
      for (Descriptor descriptor : fileDescriptor.getMessageTypes()) {
        OptMessageOpts opt = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
        if (isNotEmpty(opt.getSubType())) {
          baseTypes.put(descriptor.getName(), descriptor);
        }
        if (isNotEmpty(opt.getExtends()) && !opt.getSubValuesList().isEmpty()) {
          subTypes.computeIfAbsent(opt.getExtends(), (k) -> new ArrayList<>()).add(descriptor);
        }
      }
    }
    Map<String, String> fileMap = new HashMap<>();
    for (Entry<String, List<Descriptor>> entry : subTypes.entrySet()) {
      String content = genContent(entry.getKey(), entry.getValue());
      fileMap.put(entry.getKey() + "FromAlias.ts", content);
    }
    return fileMap;
  }

  private String genContent(String baseType, List<Descriptor> descriptors) {
    Imports imports = new Imports();
    Map<String, Object> data = new HashMap<>();
    Descriptor baseDescriptor = baseTypes.get(baseType);
    if (baseDescriptor == null) {
      throw new RuntimeException("Base type not found: " + baseType);
    }
    data.put("base_type", imports.importType(baseDescriptor.getFullName()));
    List<Map<String, Object>> types = new ArrayList<>();
    for (Descriptor descriptor : descriptors) {
      Map<String, Object> typeData = new HashMap<>();
      types.add(typeData);
      typeData.put("name", imports.importType(descriptor.getFullName()));
      typeData.put("sub_values", getSubValues(imports, descriptor));
    }
    data.put("imports", imports.toList());
    data.put("types", types);
    return new Templates().process("from.alias.ftl", data);
  }

  private List<String> getSubValues(Imports imports, Descriptor descriptor) {
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    return opts.getSubValuesList().stream()
        .map(
            subValue -> {
              EnumValueDescriptor valueDescriptor =
                  DescriptorUtils.resolveEnumValue(singletonList(descriptor.getFile()), subValue);
              if (valueDescriptor != null) {
                return imports.importType(valueDescriptor.getFullName());
              }
              return '"' + subValue + '"';
            })
        .collect(Collectors.toList());
  }
}
