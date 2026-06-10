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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

@DisplayName("EnumerationDeserializer")
class EnumerationDeserializerTest {

  @Test
  @DisplayName("should deserialize integer enum values when json contains known numbers")
  void shouldDeserializeIntegerEnumValuesWhenJsonContainsKnownNumbers() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When / Then
    assertThat(objectMapper.readValue("1", IntegerEnum.class)).isEqualTo(IntegerEnum.A);
    assertThat(objectMapper.readValue("2", IntegerEnum.class)).isEqualTo(IntegerEnum.B);
    assertThat(objectMapper.readValue("3", IntegerEnum.class)).isNull();
  }

  @Test
  @DisplayName("should deserialize string enum values when json contains known strings")
  void shouldDeserializeStringEnumValuesWhenJsonContainsKnownStrings() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When / Then
    assertThat(objectMapper.readValue("\"val_a\"", StringEnum.class)).isEqualTo(StringEnum.A);
    assertThat(objectMapper.readValue("\"val_b\"", StringEnum.class)).isEqualTo(StringEnum.B);
    assertThat(objectMapper.readValue("\"val_c\"", StringEnum.class)).isNull();
  }
}
