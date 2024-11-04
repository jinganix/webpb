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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.ts.utils.TsUtils;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import io.github.jinganix.webpb.utilities.utils.Templates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/** Generator for {@link Descriptor}. */
public class ExtendsGenerator {

  public Map<String, String> generate(RequestContext request) {
    Map<String, List<Descriptor>> extendsMap = new HashMap<>();
    for (FileDescriptor fileDescriptor : request.getTargetDescriptors()) {
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
    Map<String, Object> data = new HashMap<>();
    Set<String> packages = new HashSet<>();
    List<String> types = new ArrayList<>();
    for (Descriptor descriptor : descriptors) {
      types.add(TsUtils.toInterfaceName(descriptor.getFullName()));
      packages.add(descriptor.getFile().getPackage());
    }
    List<String> imports =
        packages.stream()
            .map(x -> "import * as " + x + " from \"./" + x + "\";")
            .sorted()
            .collect(Collectors.toList());
    data.put("types_name", typeName + "Types");
    data.put("imports", imports);
    data.put("types", types);
    return new Templates().process("file.types.ftl", data);
  }
}
