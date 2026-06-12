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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

@DisplayName("EnumerationSerializer")
class EnumerationSerializerTest {

  private EnumerationSerializer serializer;

  private JsonGenerator jsonGenerator;

  private SerializationContext serializationContext;

  @BeforeEach
  void setup() {
    serializer = new EnumerationSerializer();
    jsonGenerator = mock(JsonGenerator.class);
    serializationContext = mock(SerializationContext.class);
  }

  @Test
  @DisplayName("should write number when integer enumeration")
  void shouldWriteNumberWhenIntegerEnumeration() throws Exception {
    // When
    serializer.serialize(IntegerEnum.A, jsonGenerator, serializationContext);

    // Then
    verify(jsonGenerator).writeNumber(1);
  }

  @Test
  @DisplayName("should write string when string enumeration")
  void shouldWriteStringWhenStringEnumeration() throws Exception {
    // When
    serializer.serialize(StringEnum.A, jsonGenerator, serializationContext);

    // Then
    verify(jsonGenerator).writeString("val_a");
  }

  @Test
  @DisplayName("should write number when long enumeration")
  void shouldWriteNumberWhenLongEnumeration() throws Exception {
    // When
    serializer.serialize(LongEnum.A, jsonGenerator, serializationContext);

    // Then
    verify(jsonGenerator).writeNumber(1L);
  }

  @Test
  @DisplayName("should throw illegal argument when unsupported value type")
  void shouldThrowIllegalArgumentWhenUnsupportedValueType() {
    // Given
    @SuppressWarnings("unchecked")
    Enumeration<Double> unsupported = mock(Enumeration.class);
    when(unsupported.getValue()).thenReturn(1.0);

    // When / Then
    assertThatThrownBy(() -> serializer.serialize(unsupported, jsonGenerator, serializationContext))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
