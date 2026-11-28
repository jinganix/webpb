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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.model.BodyQueryRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@DisplayName("WebpbReactiveRequestBodyAdvice")
class WebpbReactiveRequestBodyAdviceTest {

  @Test
  @DisplayName("should decode webpb message types")
  void shouldDecodeWebpbMessageTypes() {
    // Given
    WebpbReactiveRequestBodyAdvice advice = new WebpbReactiveRequestBodyAdvice();

    // When / Then
    assertThat(
            advice.canDecode(
                ResolvableType.forClass(BodyQueryRequest.class), MediaType.APPLICATION_JSON))
        .isTrue();
    assertThat(advice.canDecode(ResolvableType.forClass(String.class), MediaType.APPLICATION_JSON))
        .isFalse();
  }

  @Test
  @DisplayName("should merge query variables after decoding body")
  void shouldMergeQueryVariablesAfterDecodingBody() {
    // Given
    WebpbReactiveRequestBodyAdvice advice = new WebpbReactiveRequestBodyAdvice();
    MockServerHttpRequest request =
        MockServerHttpRequest.post("/posts/42?tenantOnly=true")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"name\":\"demo\"}");
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    exchange
        .getAttributes()
        .put(
            org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            java.util.Map.of("id", "42"));

    // When
    BodyQueryRequest decoded =
        advice
            .decodeToMono(
                request.getBody(),
                ResolvableType.forClass(BodyQueryRequest.class),
                MediaType.APPLICATION_JSON,
                null)
            .contextWrite(
                context -> context.put(WebpbExchangeWebFilter.EXCHANGE_CONTEXT_KEY, exchange))
            .map(BodyQueryRequest.class::cast)
            .block();

    // Then
    assertThat(decoded).isNotNull();
    assertThat(decoded.getName()).isEqualTo("demo");
    assertThat(decoded.getId()).isEqualTo(42);
    assertThat(decoded.isTenantOnly()).isTrue();
  }

  @Test
  @DisplayName(
      "should signal ServerWebInputException when query value is invalid (?id=not-a-number)")
  void shouldSignalServerWebInputExceptionWhenQueryValueIsInvalid() {
    // Given: ?id=not-a-number cannot bind to long id, MVC parity is HTTP 400
    WebpbReactiveRequestBodyAdvice advice = new WebpbReactiveRequestBodyAdvice();
    MockServerHttpRequest request =
        MockServerHttpRequest.post("/posts/42?id=not-a-number")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"name\":\"demo\"}");
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    exchange
        .getAttributes()
        .put(
            org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            Collections.emptyMap());

    // When: decodeToMono must defer the failure (Mono.deferContextual factory throw -> onError)
    var mono =
        advice
            .decodeToMono(
                request.getBody(),
                ResolvableType.forClass(BodyQueryRequest.class),
                MediaType.APPLICATION_JSON,
                null)
            .contextWrite(
                context -> context.put(WebpbExchangeWebFilter.EXCHANGE_CONTEXT_KEY, exchange));

    // Then: error signal surfaces as reactive 400, not a synchronous throw nor 500
    assertThatThrownBy(mono::block).isInstanceOf(ServerWebInputException.class);
  }

  @Test
  @DisplayName("should signal ServerWebInputException when duplicate query keys (?id=1&id=2)")
  void shouldSignalServerWebInputExceptionWhenDuplicateQueryKeys() {
    // Given: duplicate single-value query keys must fail like MVC/WebpbRequestBodySupport
    WebpbReactiveRequestBodyAdvice advice = new WebpbReactiveRequestBodyAdvice();
    MockServerHttpRequest request =
        MockServerHttpRequest.post("/posts/42?id=1&id=2")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"name\":\"demo\"}");
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    exchange
        .getAttributes()
        .put(
            org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            Collections.emptyMap());

    // When: deferred mergeBody failure must be an onError signal
    var mono =
        advice
            .decodeToMono(
                request.getBody(),
                ResolvableType.forClass(BodyQueryRequest.class),
                MediaType.APPLICATION_JSON,
                null)
            .contextWrite(
                context -> context.put(WebpbExchangeWebFilter.EXCHANGE_CONTEXT_KEY, exchange));

    // Then
    assertThatThrownBy(mono::block).isInstanceOf(ServerWebInputException.class);
  }

  @Test
  @DisplayName("should not decode non webpb message types")
  void shouldNotDecodeNonWebpbMessageTypes() {
    // Given
    WebpbReactiveRequestBodyAdvice advice = new WebpbReactiveRequestBodyAdvice();

    // When / Then
    assertThat(
            advice.canDecode(
                ResolvableType.forClass(WebpbMessage.class), MediaType.APPLICATION_JSON))
        .isFalse();
    assertThat(
            advice.canDecode(ResolvableType.forClass(FooRequest.class), MediaType.APPLICATION_JSON))
        .isTrue();
  }
}
