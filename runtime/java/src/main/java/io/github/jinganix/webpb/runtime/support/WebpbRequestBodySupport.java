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

package io.github.jinganix.webpb.runtime.support;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import java.util.Map;
import org.springframework.util.CollectionUtils;

/** Shared helpers to merge path and query variables into a deserialized request body. */
public final class WebpbRequestBodySupport {

  private WebpbRequestBodySupport() {}

  /**
   * Merge path and query variables into a {@link WebpbMessage} request body.
   *
   * @param body deserialized request body
   * @param variableMap path and query variables
   * @param <T> request type
   * @return merged request body
   */
  @SuppressWarnings("unchecked")
  public static <T> T mergeBody(T body, Map<String, String> variableMap) {
    if (body == null
        || CollectionUtils.isEmpty(variableMap)
        || !(body instanceof WebpbMessage message)) {
      return body;
    }
    return (T) WebpbUtils.updateMessage(message, variableMap);
  }
}
