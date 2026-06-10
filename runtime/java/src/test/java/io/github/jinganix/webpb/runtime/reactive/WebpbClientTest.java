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

package io.github.jinganix.webpb.runtime.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.github.jinganix.webpb.runtime.WebpbMeta;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import io.github.jinganix.webpb.runtime.model.FooResponse;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@DisplayName("WebpbClient")
class WebpbClientTest {

  private final WebClient.Builder builder =
      WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

  @Test
  @DisplayName("should return response when request succeeds")
  void shouldReturnResponseWhenRequestSucceeds() {
    // Given
    WebClient webClient =
        WebClient.builder()
            .exchangeFunction(
                clientRequest ->
                    Mono.just(
                        ClientResponse.create(HttpStatus.OK).body("{\"id\": \"123\"}").build()))
            .build();
    WebpbUtils.clearContextCache();
    WebpbClient client = new WebpbClient(webClient);

    // When
    FooResponse response = client.request(new FooRequest(), FooResponse.class);

    // Then
    assertThat(response.getId()).isEqualTo(123);
  }

  @Test
  @DisplayName("should throw when response has error status")
  void shouldThrowWhenResponseHasErrorStatus() {
    // Given
    WebClient webClient =
        WebClient.builder()
            .exchangeFunction(
                clientRequest -> Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST).build()))
            .build();
    WebpbUtils.clearContextCache();
    WebpbClient client = new WebpbClient(webClient);

    // When / Then
    assertThatThrownBy(() -> client.request(new FooRequest(), FooResponse.class))
        .isInstanceOf(WebClientResponseException.class);
  }

  @Test
  @DisplayName("should throw when request message has no context")
  void shouldThrowWhenRequestMessageHasNoContext() {
    // Given
    WebpbUtils.clearContextCache();
    WebpbClient client = new WebpbClient(mock(WebClient.class));

    // When / Then
    assertThatThrownBy(() -> client.request(new BadRequest(), FooResponse.class))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should throw when request message has no method")
  void shouldThrowWhenRequestMessageHasNoMethod() {
    // Given
    WebpbUtils.clearContextCache();
    WebpbClient client = new WebpbClient(mock(WebClient.class));
    FooRequest request = new FooRequest(WebpbMeta.builder().path("path").build());

    // When / Then
    assertThatThrownBy(() -> client.request(request, FooResponse.class))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should throw when request message has no path")
  void shouldThrowWhenRequestMessageHasNoPath() {
    // Given
    WebpbUtils.clearContextCache();
    WebpbClient client = new WebpbClient(mock(WebClient.class));
    FooRequest request = new FooRequest(WebpbMeta.builder().method("method").build());

    // When / Then
    assertThatThrownBy(() -> client.request(request, FooResponse.class))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should format url when no base url is provided")
  void shouldFormatUrlWhenNoBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();
    WebpbClient webpbClient = new WebpbClient(builder.build());

    // When
    String url = webpbClient.formatUrl(new FooRequest());

    // Then
    assertThat(url).isEqualTo("/domain/123/action?p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when base url is provided")
  void shouldFormatUrlWhenBaseUrlIsProvided() throws MalformedURLException {
    // Given
    WebpbUtils.clearContextCache();
    WebpbClient webpbClient = new WebpbClient(builder.build());

    // When
    String url = webpbClient.formatUrl(new URL("https://abc"), new FooRequest());

    // Then
    assertThat(url).isEqualTo("https://abc/domain/123/action?p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should throw when path is absolute url and base url is provided")
  void shouldThrowWhenPathIsAbsoluteUrlAndBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();
    WebpbClient webpbClient = new WebpbClient(builder.build());
    FooRequest request =
        new FooRequest(WebpbMeta.builder().method("GET").path("https://domain").build());

    // When / Then
    assertThatThrownBy(() -> webpbClient.formatUrl(new URL("https://abc"), request))
        .isInstanceOf(RuntimeException.class);
  }
}
