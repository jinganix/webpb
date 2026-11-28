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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.servlet.HandlerMapping;

@DisplayName("ReactiveVariablesResolver")
class ReactiveVariablesResolverTest {

  @Test
  @DisplayName("should return merged variables from exchange")
  void shouldReturnMergedVariablesFromExchange() {
    // Given
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.post("/posts/42?tenantOnly=true").build());
    exchange
        .getAttributes()
        .put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("id", "42"));

    // When
    Map<String, List<String>> variables = ReactiveVariablesResolver.getVariableMap(exchange);

    // Then
    assertThat(variables)
        .containsEntry("id", List.of("42"))
        .containsEntry("tenantOnly", List.of("true"));
  }

  @Test
  @DisplayName("should return empty map when exchange is null")
  void shouldReturnEmptyMapWhenExchangeIsNull() {
    // When / Then
    assertThat(ReactiveVariablesResolver.getVariableMap((MockServerWebExchange) null)).isEmpty();
  }

  @Test
  @DisplayName("should prefer path variable when query collides on GET /domain/42/action?id=999")
  void shouldPreferPathVariableWhenQueryCollides() {
    // Given path id=42 and colliding query id=999 (MVC WebpbRequestUtils parity, reactive exchange
    // level)
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/domain/42/action?id=999").build());
    exchange
        .getAttributes()
        .put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("id", "42"));

    // When
    Map<String, List<String>> variables = ReactiveVariablesResolver.getVariableMap(exchange);

    // Then path value wins and is used for the message field
    assertThat(variables.get("id")).containsExactly("42");
    io.github.jinganix.webpb.runtime.model.FooRequest message =
        io.github.jinganix.webpb.runtime.WebpbUtils.updateMessage(
            new io.github.jinganix.webpb.runtime.model.FooRequest(), variables);
    assertThat(message.getId()).isEqualTo(42);
  }

  @Test
  @DisplayName("should preserve all values when duplicate query keys exist")
  void shouldPreserveAllValuesWhenDuplicateQueryKeysExist() {
    // Given
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/posts/42?id=1&id=2").build());
    exchange
        .getAttributes()
        .put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());

    // When
    Map<String, List<String>> variables = ReactiveVariablesResolver.getVariableMap(exchange);

    // Then
    assertThat(variables.get("id")).containsExactly("1", "2");
  }
}
