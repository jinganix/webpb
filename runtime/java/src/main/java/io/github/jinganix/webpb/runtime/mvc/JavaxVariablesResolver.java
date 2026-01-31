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

import static io.github.jinganix.webpb.runtime.mvc.WebpbRequestUtils.mergeVariables;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

/** Variables resolver. */
public class JavaxVariablesResolver {

  private JavaxVariablesResolver() {}

  /**
   * getVariableMap.
   *
   * @return map of variables.
   */
  public static Map<String, String> getVariableMap() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      HttpServletRequest request =
          (HttpServletRequest) ((ServletRequestAttributes) requestAttributes).getRequest();
      @SuppressWarnings("unchecked")
      Map<String, String> attributes =
          (Map<String, String>)
              request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      Map<String, String[]> parameters = request.getParameterMap();
      return mergeVariables(attributes, parameters);
    }
    return Collections.emptyMap();
  }
}
