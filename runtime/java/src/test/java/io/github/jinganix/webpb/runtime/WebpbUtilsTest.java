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
import java.util.List;
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

    // When
    String url = WebpbUtils.formatUrl(new URL("https://abc"), objectMapper, new FooRequest());

    // Then
    assertThat(url).isEqualTo("https://abc/domain/123/action?p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when pageable fields are null")
  void shouldFormatUrlWhenPageableFieldsAreNull() throws MalformedURLException {
    // Given

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

    // When
    String url = WebpbUtils.formatUrl(null, objectMapper, new FooRequest());

    // Then
    assertThat(url).isEqualTo("/domain/123/action?p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when base url already has query parameters")
  void shouldFormatUrlWhenBaseUrlAlreadyHasQueryParameters() throws MalformedURLException {
    // Given

    // When
    String url = WebpbUtils.formatUrl(new URL("https://a?a=1"), objectMapper, new FooRequest());

    // Then
    assertThat(url).isEqualTo("https://a/domain/123/action?a=1&p=true&size=20&page=10");
  }

  @Test
  @DisplayName("should format url when path contains only query parameters")
  void shouldFormatUrlWhenPathContainsOnlyQueryParameters() {
    // Given

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

    // When / Then
    assertThatThrownBy(() ->
            WebpbUtils.formatUrl(
                null,
                objectMapper,
                new FooRequest(WebpbMeta.builder().method("GET").path("https://a/{b}/c").build())))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should update message fields when parameters are provided")
  void shouldUpdateMessageFieldsWhenParametersAreProvided() {
    // Given
    Map<String, List<String>> map = new HashMap<>();
    map.put("id", List.of("12345678"));
    map.put("size", List.of("111"));
    map.put("page", List.of("222"));
    map.put("fake1", null);

    // When
    FooRequest message = WebpbUtils.updateMessage(new FooRequest(), map);

    // Then
    assertThat(message.getId()).isEqualTo(12345678);
    assertThat(message.getPageable().getSize()).isEqualTo(111);
  }

  @Test
  @DisplayName("should build array node when collection field has duplicate keys")
  void shouldBuildArrayNodeWhenCollectionFieldHasDuplicateKeys() {
    // Given
    Map<String, List<String>> map = Map.of("tag", List.of("a", "b", "c"));

    // When
    io.github.jinganix.webpb.runtime.model.CollectionRequest message =
        WebpbUtils.updateMessage(
            new io.github.jinganix.webpb.runtime.model.CollectionRequest(), map);

    // Then
    assertThat(message.getTags()).containsExactly("a", "b", "c");
  }

  @Test
  @DisplayName("should throw when duplicate keys for single-value field")
  void shouldThrowWhenDuplicateKeysForSingleValueField() {
    // Given
    Map<String, List<String>> map = Map.of("id", List.of("1", "2"));

    // When / Then
    assertThatThrownBy(() -> WebpbUtils.updateMessage(new FooRequest(), map))
        .isInstanceOf(IllegalArgumentException.class);
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

  @Test
  @DisplayName("should isolate context cache when same class has different meta")
  void shouldIsolateContextCacheWhenSameClassHasDifferentMeta() {
    // Given no cache clearing between requests with different meta
    FooRequest defaultRequest = new FooRequest();
    FooRequest customRequest =
        new FooRequest(WebpbMeta.builder().method("GET").path("/other/{id}").build());

    // When
    String defaultUrl = WebpbUtils.formatUrl(null, objectMapper, defaultRequest);
    String customUrl = WebpbUtils.formatUrl(null, objectMapper, customRequest);
    String defaultUrlAgain = WebpbUtils.formatUrl(null, objectMapper, new FooRequest());

    // Then each meta resolves its own path without cross-contamination
    assertThat(defaultUrl).isEqualTo("/domain/123/action?p=true&size=20&page=10");
    assertThat(customUrl).isEqualTo("/other/123");
    assertThat(defaultUrlAgain).isEqualTo(defaultUrl);
    assertThat(WebpbUtils.getContext(defaultRequest).getPath()).contains("/domain/");
    assertThat(WebpbUtils.getContext(customRequest).getPath()).isEqualTo("/other/{id}");
  }

  @Test
  @DisplayName("should encode path segment when value contains special characters")
  void shouldEncodePathSegmentWhenValueContainsSpecialCharacters() {
    // Given
    FooRequest request =
        new FooRequest(WebpbMeta.builder().method("GET").path("/files/{data}").build())
            .setData("a b/\u4e2d\u6587");

    // When
    String url = WebpbUtils.formatUrl(null, objectMapper, request);

    // Then space, slash and Chinese are percent-encoded as a single segment
    assertThat(url).isEqualTo("/files/a%20b%2F%E4%B8%AD%E6%96%87");
  }

  @Test
  @DisplayName("should encode query value when value contains special characters")
  void shouldEncodeQueryValueWhenValueContainsSpecialCharacters() {
    // Given
    FooRequest request =
        new FooRequest(WebpbMeta.builder().method("GET").path("/search?q={data}").build())
            .setData("a b&c\u4e2d\u6587");

    // When
    String url = WebpbUtils.formatUrl(null, objectMapper, request);

    // Then space, ampersand and Chinese are percent-encoded in query value
    assertThat(url).isEqualTo("/search?q=a%20b%26c%E4%B8%AD%E6%96%87");
  }
}
