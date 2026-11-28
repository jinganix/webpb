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

package io.github.jinganix.webpb.runtime.mvc;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Utilities to update a {@link WebpbMessage}. */
public class WebpbRequestUtils {

  private WebpbRequestUtils() {}

  /**
   * Merge attributes and parameterMap to a variablesMap.
   *
   * @param attributes map of attributes
   * @param parameters multimap of parameters
   * @return map of variables
   */
  public static Map<String, List<String>> mergeVariables(
      Map<String, String> attributes, Map<String, String[]> parameters) {
    Map<String, List<String>> variablesMap = new HashMap<>();
    if (attributes != null) {
      for (Map.Entry<String, String> entry : attributes.entrySet()) {
        if (entry.getValue() != null) {
          variablesMap.put(entry.getKey(), List.of(entry.getValue()));
        }
      }
    }
    if (parameters != null) {
      for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
        if (entry.getValue() != null && entry.getValue().length > 0) {
          List<String> values = new ArrayList<>();
          for (String value : entry.getValue()) {
            if (value != null) {
              values.add(value);
            }
          }
          if (!values.isEmpty()) {
            variablesMap.putIfAbsent(entry.getKey(), List.copyOf(values));
          }
        }
      }
    }
    return variablesMap;
  }
}
