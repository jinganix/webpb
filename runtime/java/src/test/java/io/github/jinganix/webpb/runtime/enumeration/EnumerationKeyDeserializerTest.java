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

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@DisplayName("EnumerationKeyDeserializer")
class EnumerationKeyDeserializerTest {

  @Test
  @DisplayName("should deserialize integer enum keys when json map uses numeric keys")
  void shouldDeserializeIntegerEnumKeysWhenJsonMapUsesNumericKeys() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When
    Map<IntegerEnum, Integer> data =
        objectMapper.readValue("{\"1\":1,\"2\":2}", new TypeReference<>() {});

    // Then
    assertThat(data).hasSize(2);
    assertThat(data.get(IntegerEnum.A)).isEqualTo(1);
    assertThat(data.get(IntegerEnum.B)).isEqualTo(2);
  }

  @Test
  @DisplayName("should deserialize string enum keys when json map uses string keys")
  void shouldDeserializeStringEnumKeysWhenJsonMapUsesStringKeys() {
    // Given
    ObjectMapper objectMapper = new ObjectMapper();

    // When
    Map<StringEnum, Integer> data =
        objectMapper.readValue(
            "{\"val_a\":1,\"val_b\":2}", new TypeReference<Map<StringEnum, Integer>>() {});

    // Then
    assertThat(data).hasSize(2);
    assertThat(data.get(StringEnum.A)).isEqualTo(1);
    assertThat(data.get(StringEnum.B)).isEqualTo(2);
  }
}
