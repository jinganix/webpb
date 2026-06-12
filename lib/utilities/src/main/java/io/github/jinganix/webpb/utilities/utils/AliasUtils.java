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
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    List<Descriptor> messages = new ArrayList<>(OptionUtils.getExtendedMessages(descriptor));
    messages.add(descriptor);

    Set<String> names = new HashSet<>();
    for (Descriptor messageDescriptor : messages) {
      for (FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
        names.add(fieldDescriptor.getName());
      }
    }

    Map<String, String> aliases = new TreeMap<>();
    Set<String> usedAliases = new HashSet<>();
    int offset = 0;
    for (Descriptor messageDescriptor : messages) {
      List<FieldDescriptor> fields = messageDescriptor.getFields();
      int maxIndex = offset;
      for (FieldDescriptor fieldDescriptor : fields) {
        int index = offset + fieldDescriptor.getNumber() - 1;
        String alias = Utils.toBase52(index);
        while (names.contains(alias) || usedAliases.contains(alias)) {
          index++;
          alias = Utils.toBase52(index);
        }
        aliases.put(fieldDescriptor.getName(), alias);
        usedAliases.add(alias);
        maxIndex = Math.max(maxIndex, index);
      }
      int reserve =
          OptionUtils.getOpts(messageDescriptor, MessageOpts::hasTs).getTs().getAliasReserve();
      offset = Math.max(maxIndex, reserve);
    }
    return aliases;
  }

  /**
   * Throw an exception when alias_reserve is not greater than the max field number.
   *
   * @param descriptor {@link Descriptor}
   */
  public static void checkAliasReserve(Descriptor descriptor) {
    List<Descriptor> messages = new ArrayList<>(OptionUtils.getExtendedMessages(descriptor));
    messages.add(descriptor);
    for (Descriptor messageDescriptor : messages) {
      int reserve =
          OptionUtils.getOpts(messageDescriptor, MessageOpts::hasTs).getTs().getAliasReserve();
      if (reserve == 0) {
        continue;
      }
      int maxNumber = 0;
      for (FieldDescriptor fieldDescriptor : messageDescriptor.getFields()) {
        maxNumber = Math.max(maxNumber, fieldDescriptor.getNumber());
      }
      if (reserve <= maxNumber) {
        throw new RuntimeException(
            String.format(
                "`alias_reserve` must be greater than max field number %d in message `%s`",
                maxNumber, messageDescriptor.getFullName()));
      }
    }
  }
}
