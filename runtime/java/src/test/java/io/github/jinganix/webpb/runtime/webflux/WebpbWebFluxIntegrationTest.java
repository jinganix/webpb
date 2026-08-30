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

import io.github.jinganix.webpb.runtime.model.BodyQueryController;
import io.github.jinganix.webpb.runtime.model.BodyQueryRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import io.github.jinganix.webpb.runtime.model.GetFooController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@DisplayName("WebpbWebFluxIntegrationTest")
class WebpbWebFluxIntegrationTest {

  private BodyQueryController bodyQueryController;

  private GetFooController getFooController;

  private WebTestClient client;

  @BeforeEach
  void setUp() {
    bodyQueryController = new BodyQueryController();
    getFooController = new GetFooController();
    client =
        WebTestClient.bindToController(bodyQueryController, getFooController)
            .webFilter(new WebpbExchangeWebFilter())
            .argumentResolvers(
                configurer ->
                    configurer.addCustomResolver(new WebpbReactiveHandlerMethodArgumentResolver()))
            .httpMessageCodecs(
                configurer ->
                    configurer.customCodecs().register(new WebpbReactiveRequestBodyAdvice()))
            .build();
  }

  @Test
  @DisplayName("should merge body and query parameters for request body")
  void shouldMergeBodyAndQueryParametersForRequestBody() {
    // When / Then
    client
        .post()
        .uri("/posts/42?tenantOnly=true")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"name\":\"demo\"}")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(BodyQueryRequest.class)
        .consumeWith(
            result -> {
              BodyQueryRequest request = result.getResponseBody();
              assertThat(request).isNotNull();
              assertThat(request.getName()).isEqualTo("demo");
              assertThat(request.getId()).isEqualTo(42);
              assertThat(request.isTenantOnly()).isTrue();
            });
  }

  @Test
  @DisplayName("should resolve query only request without request body annotation")
  void shouldResolveQueryOnlyRequestWithoutRequestBodyAnnotation() {
    // When / Then
    client
        .get()
        .uri("/domain/42/action?pagination=true&page=1&size=5")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(FooRequest.class)
        .consumeWith(
            result -> {
              FooRequest request = result.getResponseBody();
              assertThat(request).isNotNull();
              assertThat(request.getId()).isEqualTo(42);
              assertThat(request.isPagination()).isTrue();
              assertThat(request.getPageable().getPage()).isEqualTo(1);
              assertThat(request.getPageable().getSize()).isEqualTo(5);
            });
  }
}
