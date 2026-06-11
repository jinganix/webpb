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

package io.github.jinganix.webpb.runtime.mvc;

import static io.github.jinganix.webpb.runtime.mvc.WebpbRequestUtils.mergeVariables;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebpbRequestUtils")
class WebpbRequestUtilsTest {

  @Test
  @DisplayName("should return empty map when both inputs are empty")
  void shouldReturnEmptyMapWhenBothInputsAreEmpty() {
    // When
    Map<String, String> result = mergeVariables(Collections.emptyMap(), Collections.emptyMap());

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("should include attributes when attribute map is not empty")
  void shouldIncludeAttributesWhenAttributeMapIsNotEmpty() {
    // Given
    Map<String, String> attributes = new HashMap<>();
    attributes.put("a", "b");

    // When
    Map<String, String> result = mergeVariables(attributes, Collections.emptyMap());

    // Then
    assertThat(result).hasSize(1).containsKey("a");
  }

  @Test
  @DisplayName("should include parameters when parameter map is not empty")
  void shouldIncludeParametersWhenParameterMapIsNotEmpty() {
    // Given
    Map<String, String[]> parameters = new HashMap<>();
    parameters.put("a", new String[] {"b"});

    // When
    Map<String, String> result = mergeVariables(Collections.emptyMap(), parameters);

    // Then
    assertThat(result).hasSize(1).containsKey("a");
  }

  @Test
  @DisplayName("should merge attributes and parameters when both are not empty")
  void shouldMergeAttributesAndParametersWhenBothAreNotEmpty() {
    // Given
    Map<String, String> attributes = new HashMap<>();
    attributes.put("a", "b");

    Map<String, String[]> parameters = new HashMap<>();
    parameters.put("c", new String[] {"d"});

    // When
    Map<String, String> result = mergeVariables(attributes, parameters);

    // Then
    assertThat(result).hasSize(2).containsKeys("a", "c");
  }
}
