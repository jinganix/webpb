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

package io.github.jinganix.webpb.runtime.enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

@DisplayName("EnumerationSerializer")
class EnumerationSerializerTest {

  @Test
  @DisplayName("should serialize integer enum values when enum constants are known")
  void shouldSerializeIntegerEnumValuesWhenEnumConstantsAreKnown() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When / Then
    assertThat(objectMapper.writeValueAsString(IntegerEnum.A)).isEqualTo("1");
    assertThat(objectMapper.writeValueAsString(IntegerEnum.B)).isEqualTo("2");
  }

  @Test
  @DisplayName("should serialize string enum values when enum constants are known")
  void shouldSerializeStringEnumValuesWhenEnumConstantsAreKnown() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When / Then
    assertThat(objectMapper.writeValueAsString(StringEnum.A)).isEqualTo("\"val_a\"");
    assertThat(objectMapper.writeValueAsString(StringEnum.B)).isEqualTo("\"val_b\"");
  }

  @Test
  @DisplayName("should throw when serializing unhandled enum type")
  void shouldThrowWhenSerializingUnhandledEnumType() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When / Then
    assertThatThrownBy(() -> objectMapper.writeValueAsString(LongEnum.A))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
