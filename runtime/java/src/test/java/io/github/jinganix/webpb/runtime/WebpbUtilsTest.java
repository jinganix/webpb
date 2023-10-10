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

package io.github.jinganix.webpb.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import io.github.jinganix.webpb.runtime.model.Pageable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WebpbUtilsTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReadWebpbMetaSuccess() {
    assertNotNull(WebpbUtils.readWebpbMeta(FooRequest.class));
  }

  @Test
  void shouldReturnNullWhenWepbMetaNotExists() {
    assertNull(WebpbUtils.readWebpbMeta(BadRequest.class));
  }

  @Test
  void isValidPathTest() {
    assertFalse(WebpbUtils.isValidPath(null));
    assertFalse(WebpbUtils.isValidPath("//"));
    assertFalse(WebpbUtils.isValidPath("abc"));
    assertTrue(WebpbUtils.isValidPath(""));
    assertTrue(WebpbUtils.isValidPath("/ /"));
    assertTrue(WebpbUtils.isValidPath("/"));
    assertTrue(WebpbUtils.isValidPath("/abc"));
    assertTrue(WebpbUtils.isValidPath("https://abc"));
    assertTrue(WebpbUtils.isValidPath("https://a/{b}"));
  }

  @Test
  void shouldFormatUrlSuccessWhenWithBaseUrl() throws MalformedURLException {
    WebpbUtils.clearContextCache();
    String url = WebpbUtils.formatUrl(new URL("https://abc"), objectMapper, new FooRequest());
    assertEquals("https://abc/domain/123/action?p=true&size=20&page=10", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenWithoutPageable() throws MalformedURLException {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            new URL("https://abc"),
            objectMapper,
            new FooRequest().setPageable(new Pageable().setPage(null).setSize(null)));
    assertEquals("https://abc/domain/123/action?p=true", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenWithoutBaseUrl() {
    WebpbUtils.clearContextCache();
    String url = WebpbUtils.formatUrl(null, objectMapper, new FooRequest());
    assertEquals("/domain/123/action?p=true&size=20&page=10", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenBaseUrlWithQuery() throws MalformedURLException {
    WebpbUtils.clearContextCache();
    String url = WebpbUtils.formatUrl(new URL("https://a?a=1"), objectMapper, new FooRequest());
    assertEquals("https://a/domain/123/action?a=1&p=true&size=20&page=10", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenOnlyQuery() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(
                WebpbMeta.builder()
                    .method("GET")
                    .path("/pagination={pagination}&size={pageable.size}&page={pageable.page}")
                    .build()));
    assertEquals("?pagination=true&size=20&page=10", url);
  }

  @Test
  void shouldFormatUrlSuccessGivenPathIsUrlWhenWithBaseUrl() throws MalformedURLException {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            new URL("https://domain"),
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("/").build()));
    assertEquals("https://domain", url);
  }

  @Test
  void shouldFormatUrlSuccessGivenWithContext() throws MalformedURLException {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            new URL("https://domain"),
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").context("ctx").path("/").build()));
    assertEquals("https://domain/ctx", url);
  }

  @Test
  void shouldFormatUrlSuccessGivenWithoutBaseUrl() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            new FooRequest(WebpbMeta.builder().method("GET").context("ctx").path("/a/b").build()));
    assertEquals("/ctx/a/b", url);
  }

  @Test
  void shouldFormatUrlSuccessGivenWithoutBaseUrlAndPathIsUrl() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            new FooRequest(
                WebpbMeta.builder().method("GET").context("ctx").path("https://a.com/b").build()));
    assertEquals("https://a.com/b", url);
  }

  @Test
  void shouldFormatUrlSuccessGivenPathIsUrlWhenWithoutBaseUrl() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("https://domain").build()));
    assertEquals("https://domain", url);
  }

  @Test
  void shouldFormatUrlThrowExceptionGivenPathIsUrlWhenWithBaseUrl() {
    WebpbUtils.clearContextCache();
    FooRequest request =
        new FooRequest(WebpbMeta.builder().method("GET").path("https://domain").build());
    assertThrows(
        RuntimeException.class,
        () -> WebpbUtils.formatUrl(new URL("https://abc"), objectMapper, request));
  }

  @Test
  void shouldFormatUrlSuccessWhenRequestMissingValue() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("https://domain?a={a}").build()));
    assertEquals("https://domain", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenQueryWithSuffix() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(
                WebpbMeta.builder().method("GET").path("https://domain?data={data}hello").build()));
    assertEquals("https://domain?data={data}hello", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenQueryWithOnlySuffix() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(WebpbMeta.builder().method("GET").path("https://domain?hello").build()));
    assertEquals("https://domain?hello", url);
  }

  @Test
  void shouldFormatUrlSuccessWhenWithMultiplePathVariables() {
    WebpbUtils.clearContextCache();
    String url =
        WebpbUtils.formatUrl(
            null,
            objectMapper,
            new FooRequest(
                WebpbMeta.builder()
                    .method("GET")
                    .path("https://{pagination}/{pageable.page}/{pageable.size}")
                    .build()));
    assertEquals("https://true/10/20", url);
  }

  @Test
  void shouldThrowExceptionWhenPathVariableNotExists() {
    WebpbUtils.clearContextCache();
    assertThrows(
        RuntimeException.class,
        () ->
            WebpbUtils.formatUrl(
                null,
                objectMapper,
                new FooRequest(WebpbMeta.builder().method("GET").path("https://a/{b}/c").build())),
        "Path variable 'a' not found");
  }

  @Test
  void givenNonEmptyParameters_whenUpdateMessage_ThenReturnUpdatedMessage() {
    Map<String, String> map = new HashMap<>();
    map.put("id", "12345678");
    map.put("size", "111");
    map.put("page", "222");
    map.put("fake1", null);
    FooRequest message = WebpbUtils.updateMessage(new FooRequest(), map);
    assertEquals(12345678, message.getId());
    assertEquals(111, message.getPageable().getSize());
  }

  @Test
  void givenWebpbMessage_whenSerialize_ThenReturnString() {
    assertEquals("{\"data\":\"data123\"}", WebpbUtils.serialize(new FooRequest()));
  }

  @Test
  void givenDataString_whenDeserialize_ThenReturnMessage() {
    FooRequest message = WebpbUtils.deserialize("{\"data\":\"data123\"}", FooRequest.class);
    assertEquals("data123", message.getData());
  }
}
