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

package io.github.jinganix.webpb.utilities.utils;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Utilities to handle field alias. */
public class AliasUtils {

  private AliasUtils() {}

  /**
   * Get all auto alias mapping.
   *
   * @param descriptor {@link Descriptor}
   * @return alias mapping
   */
  public static Map<String, String> getAutoAliases(Descriptor descriptor) {
    List<FieldDescriptor> fields = OptionUtils.getAllFields(descriptor);
    Set<String> names = fields.stream().map(FieldDescriptor::getName).collect(Collectors.toSet());
    Map<String, String> aliases = new TreeMap<>();
    int index = 0;
    for (FieldDescriptor fieldDescriptor : fields) {
      String alias = Utils.toBase52(index++);
      while (names.contains(alias)) {
        alias = Utils.toBase52(index++);
      }
      aliases.put(fieldDescriptor.getName(), alias);
    }
    return aliases;
  }
}
