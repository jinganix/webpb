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

import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebpbRequestBodySupport")
class WebpbRequestBodySupportTest {

  @Test
  @DisplayName("should return original body when variable map is empty")
  void shouldReturnOriginalBodyWhenVariableMapIsEmpty() {
    // Given
    FooRequest request = new FooRequest();

    // When / Then
    assertThat(WebpbRequestBodySupport.mergeBody(request, Collections.emptyMap())).isSameAs(request);
  }

  @Test
  @DisplayName("should merge variables when message has webpb meta")
  void shouldMergeVariablesWhenMessageHasWebpbMeta() {
    // Given
    Map<String, String> variables = Map.of("id", "42");

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
    assertThat(WebpbRequestBodySupport.mergeBody(request, Map.of("id", "42"))).isSameAs(request);
  }
}
