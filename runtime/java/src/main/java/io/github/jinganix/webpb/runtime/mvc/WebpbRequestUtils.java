/*
 * Copyright (c) 2020 jinganix@gmail.com, All Rights Reserved.
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
 */

package io.github.jinganix.webpb.runtime.mvc;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import java.util.HashMap;
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
  public static Map<String, String> mergeVariables(
      Map<String, String> attributes, Map<String, String[]> parameters) {
    Map<String, String> variablesMap =
        attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      if (entry.getValue() != null && entry.getValue().length > 0) {
        variablesMap.put(entry.getKey(), entry.getValue()[0]);
      }
    }
    return variablesMap;
  }
}
