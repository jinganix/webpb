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

package io.github.jinganix.webpb.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import io.github.jinganix.webpb.runtime.model.Pageable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

@DisplayName("WebpbUtils")
class WebpbUtilsTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("should return webpb meta when message defines it")
  void shouldReturnWebpbMetaWhenMessageDefinesIt() {
    // When / Then
    assertThat(WebpbUtils.readWebpbMeta(FooRequest.class)).isNotNull();
  }

  @Test
  @DisplayName("should return null when message has no webpb meta")
  void shouldReturnNullWhenMessageHasNoWebpbMeta() {
    // When / Then
    assertThat(WebpbUtils.readWebpbMeta(BadRequest.class)).isNull();
  }

  @Test
  @DisplayName("should validate paths when given various inputs")
  void shouldValidatePathsWhenGivenVariousInputs() {
    // When / Then
    assertThat(WebpbUtils.isValidPath(null)).isFalse();
    assertThat(WebpbUtils.isValidPath("//")).isFalse();
    assertThat(WebpbUtils.isValidPath("abc")).isFalse();
    assertThat(WebpbUtils.isValidPath("")).isTrue();
    assertThat(WebpbUtils.isValidPath("/ /")).isTrue();
    assertThat(WebpbUtils.isValidPath("/")).isTrue();
    assertThat(WebpbUtils.isValidPath("/abc")).isTrue();
    assertThat(WebpbUtils.isValidPath("https://abc")).isTrue();
    assertThat(WebpbUtils.isValidPath("https://a/{b}")).isTrue();
  }

  @Test
  @DisplayName("should format url when base url is provided")
  void shouldFormatUrlWhenBaseUrlIsProvided() throws MalformedURLException {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url = WebpbUtils.formatUrl(new URL("https://abc"), objectMapper, new FooRequest());

    // Then
    assertThat(url).isEqualTo("https://abc/domain/123/action?p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when pageable fields are null")
  void shouldFormatUrlWhenPageableFieldsAreNull() throws MalformedURLException {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            new URL("https://abc"),
            objectMapper,
            new FooRequest().setPageable(new Pageable().setPage(null).setSize(null)));

    // Then
    assertThat(url).isEqualTo("https://abc/domain/123/action?p=true");
  }

  @Test
  @DisplayName("should format url when no base url is provided")
  void shouldFormatUrlWhenNoBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url = WebpbUtils.formatUrl(null, objectMapper, new FooRequest());

    // Then
    assertThat(url).isEqualTo("/domain/123/action?p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when base url already has query parameters")
  void shouldFormatUrlWhenBaseUrlAlreadyHasQueryParameters() throws MalformedURLException {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url = WebpbUtils.formatUrl(new URL("https://a?a=1"), objectMapper, new FooRequest());

    // Then
    assertThat(url).isEqualTo("https://a/domain/123/action?a=1&p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when path contains only query parameters")
  void shouldFormatUrlWhenPathContainsOnlyQueryParameters() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(
                WebpbMeta.builder()
                    .method("GET")
                    .path("/pagination={pagination}&size={pageable.size}&page={pageable.page}")
                    .build()));

    // Then
    assertThat(url).isEqualTo("?pagination=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when path is root and base url is provided")
  void shouldFormatUrlWhenPathIsRootAndBaseUrlIsProvided() throws MalformedURLException {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            new URL("https://domain"),
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("/").build()));

    // Then
    assertThat(url).isEqualTo("https://domain");
  }

  @Test
  @DisplayName("should format url when context is set and base url is provided")
  void shouldFormatUrlWhenContextIsSetAndBaseUrlIsProvided() throws MalformedURLException {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            new URL("https://domain"),
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").context("ctx").path("/").build()));

    // Then
    assertThat(url).isEqualTo("https://domain/ctx");
  }

  @Test
  @DisplayName("should format url when context is set and no base url is provided")
  void shouldFormatUrlWhenContextIsSetAndNoBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            new FooRequest(WebpbMeta.builder().method("GET").context("ctx").path("/a/b").build()));

    // Then
    assertThat(url).isEqualTo("/ctx/a/b");
  }

  @Test
  @DisplayName("should format url when path is absolute and no base url is provided")
  void shouldFormatUrlWhenPathIsAbsoluteAndNoBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            new FooRequest(
                WebpbMeta.builder().method("GET").context("ctx").path("https://a.com/b").build()));

    // Then
    assertThat(url).isEqualTo("https://a.com/b");
  }

  @Test
  @DisplayName("should format absolute path when no base url is provided")
  void shouldFormatAbsolutePathWhenNoBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("https://domain").build()));

    // Then
    assertThat(url).isEqualTo("https://domain");
  }

  @Test
  @DisplayName("should throw when path is absolute url and base url is provided")
  void shouldThrowWhenPathIsAbsoluteUrlAndBaseUrlIsProvided() {
    // Given
    WebpbUtils.clearContextCache();
    FooRequest request =
        new FooRequest(WebpbMeta.builder().method("GET").path("https://domain").build());

    // When / Then
    assertThatThrownBy(() -> WebpbUtils.formatUrl(new URL("https://abc"), objectMapper, request))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should omit missing query values when request field is absent")
  void shouldOmitMissingQueryValuesWhenRequestFieldIsAbsent() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("https://domain?a={a}").build()));

    // Then
    assertThat(url).isEqualTo("https://domain");
  }

  @Test
  @DisplayName("should preserve query suffix when placeholder has trailing text")
  void shouldPreserveQuerySuffixWhenPlaceholderHasTrailingText() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(
                WebpbMeta.builder().method("GET").path("https://domain?data={data}hello").build()));

    // Then
    assertThat(url).isEqualTo("https://domain?data={data}hello");
  }

  @Test
  @DisplayName("should preserve static query suffix when no placeholders are present")
  void shouldPreserveStaticQuerySuffixWhenNoPlaceholdersArePresent() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("https://domain?hello").build()));

    // Then
    assertThat(url).isEqualTo("https://domain?hello");
  }

  @Test
  @DisplayName("should substitute multiple path variables when all values exist")
  void shouldSubstituteMultiplePathVariablesWhenAllValuesExist() {
    // Given
    WebpbUtils.clearContextCache();

    // When
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(
                WebpbMeta.builder()
                    .method("GET")
                    .path("https://{pagination}/{pageable.page}/{pageable.size}")
                    .build()));

    // Then
    assertThat(url).isEqualTo("https://true/10/20");
  }

  @Test
  @DisplayName("should throw when path variable is not found on message")
  void shouldThrowWhenPathVariableIsNotFoundOnMessage() {
    // Given
    WebpbUtils.clearContextCache();

    // When / Then
    assertThatThrownBy(
            () ->
                WebpbUtils.formatUrl(
                    null,
                    objectMapper,
                    new FooRequest(
                        WebpbMeta.builder().method("GET").path("https://a/{b}/c").build())))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should update message fields when parameters are provided")
  void shouldUpdateMessageFieldsWhenParametersAreProvided() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put("id", "12345678");
    map.put("size", "111");
    map.put("page", "222");
    map.put("fake1", null);

    // When
    FooRequest message = WebpbUtils.updateMessage(new FooRequest(), map);

    // Then
    assertThat(message.getId()).isEqualTo(12345678);
    assertThat(message.getPageable().getSize()).isEqualTo(111);
  }

  @Test
  @DisplayName("should serialize message when webpb message is provided")
  void shouldSerializeMessageWhenWebpbMessageIsProvided() {
    // When / Then
    assertThat(WebpbUtils.serialize(new FooRequest())).isEqualTo("{\"data\":\"data123\"}");
  }

  @Test
  @DisplayName("should deserialize message when json string is provided")
  void shouldDeserializeMessageWhenJsonStringIsProvided() {
    // When
    FooRequest message = WebpbUtils.deserialize("{\"data\":\"data123\"}", FooRequest.class);

    // Then
    assertThat(message.getData()).isEqualTo("data123");
  }
}
