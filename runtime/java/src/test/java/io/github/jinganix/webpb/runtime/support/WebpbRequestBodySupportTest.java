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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import io.github.jinganix.webpb.runtime.WebpbUtils;
import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("WebpbRequestBodySupport")
class WebpbRequestBodySupportTest {

  @Test
  @DisplayName("should return original body when variable map is empty")
  void shouldReturnOriginalBodyWhenVariableMapIsEmpty() {
    // Given
    FooRequest request = new FooRequest();

    // When / Then
    assertThat(WebpbRequestBodySupport.mergeBody(request, Collections.emptyMap()))
        .isSameAs(request);
  }

  @Test
  @DisplayName("should merge variables when message has webpb meta")
  void shouldMergeVariablesWhenMessageHasWebpbMeta() {
    // Given
    Map<String, List<String>> variables = Map.of("id", List.of("42"));

    // When
    FooRequest request = WebpbRequestBodySupport.mergeBody(new FooRequest(), variables);

    // Then
    assertThat(request.getId()).isEqualTo(42);
  }

  @Test
  @DisplayName("should return original body when message has no webpb meta")
  void shouldReturnOriginalBodyWhenMessageHasNoWebpbMeta() {
    // Given
    BadRequest request = new BadRequest();

    // When / Then
    assertThat(WebpbRequestBodySupport.mergeBody(request, Map.of("id", List.of("42"))))
        .isSameAs(request);
  }

  @Test
  @DisplayName("should throw 400 when query value is invalid")
  void shouldThrowWhenQueryValueIsInvalid() {
    // Given
    Map<String, List<String>> variables = Map.of("id", List.of("not-a-number"));

    // When / Then
    assertThatThrownBy(() -> WebpbRequestBodySupport.mergeBody(new FooRequest(), variables))
        .isInstanceOf(org.springframework.web.server.ServerWebInputException.class);
  }

  @Test
  @DisplayName("should throw when duplicate keys for single-value field")
  void shouldThrowWhenDuplicateKeysForSingleValueField() {
    // Given
    Map<String, List<String>> variables = Map.of("id", List.of("1", "2"));

    // When / Then
    assertThatThrownBy(() -> WebpbRequestBodySupport.mergeBody(new FooRequest(), variables))
        .isInstanceOf(org.springframework.web.server.ServerWebInputException.class);
  }

  @Test
  @DisplayName("should rethrow server input exception without wrapping")
  void shouldRethrowServerInputExceptionWithoutWrapping() {
    // Given updateMessage throws ServerWebInputException directly (passthrough branch)
    org.springframework.web.server.ServerWebInputException expected =
        new org.springframework.web.server.ServerWebInputException("direct", null, null);
    try (MockedStatic<WebpbUtils> mocked = mockStatic(WebpbUtils.class)) {
      mocked.when(() -> WebpbUtils.updateMessage(any(), any())).thenThrow(expected);

      // When / Then the same instance passes through unwrapped
      assertThatThrownBy(
              () -> WebpbRequestBodySupport.mergeBody(new FooRequest(), Map.of("id", List.of("1"))))
          .isSameAs(expected);
    }
  }
}
