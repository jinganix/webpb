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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebpbMeta")
class WebpbMetaTest {

  private final WebpbMeta webpbMeta =
      WebpbMeta.builder().method("GET").context("user").path("/get/user").build();

  @Test
  @DisplayName("should return method when meta is built")
  void shouldReturnMethodWhenMetaIsBuilt() {
    // When / Then
    assertThat(webpbMeta.getMethod()).isEqualTo("GET");
  }

  @Test
  @DisplayName("should return context when meta is built")
  void shouldReturnContextWhenMetaIsBuilt() {
    // When / Then
    assertThat(webpbMeta.getContext()).isEqualTo("user");
  }

  @Test
  @DisplayName("should return path when meta is built")
  void shouldReturnPathWhenMetaIsBuilt() {
    // When / Then
    assertThat(webpbMeta.getPath()).isEqualTo("/get/user");
  }

  @Test
  @DisplayName("should return descriptive string when toString is called")
  void shouldReturnDescriptiveStringWhenToStringIsCalled() {
    // When / Then
    assertThat(webpbMeta.toString())
        .isEqualTo("WebpbMeta(method=GET, context=user, path=/get/user)");
  }
}
