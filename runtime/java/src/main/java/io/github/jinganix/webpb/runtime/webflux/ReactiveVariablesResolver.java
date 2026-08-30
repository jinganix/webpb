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

package io.github.jinganix.webpb.runtime.webflux;

import static io.github.jinganix.webpb.runtime.mvc.WebpbRequestUtils.mergeVariables;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.HandlerMapping;
import reactor.util.context.ContextView;

/** Resolves path and query variables from a reactive server exchange. */
public final class ReactiveVariablesResolver {

  private ReactiveVariablesResolver() {}

  /**
   * Return path and query variables from the current {@link ServerWebExchange}.
   *
   * @param exchange current exchange
   * @return map of variables
   */
  public static Map<String, String> getVariableMap(ServerWebExchange exchange) {
    if (exchange == null) {
      return Collections.emptyMap();
    }
    Map<String, String> attributes = readUriTemplateVariables(exchange);
    return mergeVariables(attributes, toParameterMap(exchange.getRequest().getQueryParams()));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> readUriTemplateVariables(ServerWebExchange exchange) {
    Map<String, String> reactive =
        (Map<String, String>)
            exchange.getAttribute(
                org.springframework.web.reactive.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (reactive != null && !reactive.isEmpty()) {
      return reactive;
    }
    Map<String, String> servlet =
        (Map<String, String>)
            exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    return servlet == null ? Collections.emptyMap() : servlet;
  }

  /**
   * Return path and query variables from the current Reactor context.
   *
   * @param contextView current context
   * @return map of variables
   */
  public static Map<String, String> getVariableMap(ContextView contextView) {
    if (contextView == null || !contextView.hasKey(WebpbExchangeWebFilter.EXCHANGE_CONTEXT_KEY)) {
      return Collections.emptyMap();
    }
    return getVariableMap((ServerWebExchange) contextView.get(WebpbExchangeWebFilter.EXCHANGE_CONTEXT_KEY));
  }

  private static Map<String, String[]> toParameterMap(MultiValueMap<String, String> queryParams) {
    if (queryParams == null || queryParams.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, String[]> parameters = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      List<String> values = entry.getValue();
      if (values != null && !values.isEmpty()) {
        parameters.put(entry.getKey(), values.toArray(String[]::new));
      }
    }
    return parameters;
  }
}
