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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("EnumerationDeserializer")
class EnumerationDeserializerTest {

  private JsonMapper jsonMapper;

  private JavaType integerEnumType;

  @BeforeEach
  void setup() {
    jsonMapper = new JsonMapper();
    integerEnumType = jsonMapper.getTypeFactory().constructType(IntegerEnum.class);
  }

  @Nested
  @DisplayName("deserialize")
  class Deserialize {

    @Test
    @DisplayName("should resolve enumeration when string value")
    void shouldResolveEnumerationWhenStringValue() throws Exception {
      // Given
      EnumerationDeserializer<IntegerEnum> deserializer = new EnumerationDeserializer<>(valueMap());

      // When
      IntegerEnum result = deserialize(deserializer, "\"1\"");

      // Then
      assertThat(result).isEqualTo(IntegerEnum.A);
    }

    @Test
    @DisplayName("should resolve enumeration when integer value")
    void shouldResolveEnumerationWhenIntegerValue() throws Exception {
      // Given
      EnumerationDeserializer<IntegerEnum> deserializer = new EnumerationDeserializer<>(valueMap());

      // When
      IntegerEnum result = deserialize(deserializer, "2");

      // Then
      assertThat(result).isEqualTo(IntegerEnum.B);
    }

    @Test
    @DisplayName("should resolve enumeration when long value")
    void shouldResolveEnumerationWhenLongValue() throws Exception {
      // Given
      EnumerationDeserializer<LongEnum> deserializer =
          new EnumerationDeserializer<>(longValueMap());

      // When
      LongEnum result = deserializeLong(deserializer, "2");

      // Then
      assertThat(result).isEqualTo(LongEnum.B);
    }

    @Test
    @DisplayName("should return null when unknown value")
    void shouldReturnNullWhenUnknownValue() throws Exception {
      // Given
      EnumerationDeserializer<IntegerEnum> deserializer = new EnumerationDeserializer<>(valueMap());

      // When
      IntegerEnum result = deserialize(deserializer, "\"unknown\"");

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("should return null when unsupported token")
    void shouldReturnNullWhenUnsupportedToken() throws Exception {
      // Given
      EnumerationDeserializer<IntegerEnum> deserializer = new EnumerationDeserializer<>(valueMap());

      // When
      IntegerEnum result = deserialize(deserializer, "true");

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("createContextual")
  class CreateContextual {

    @Test
    @DisplayName("should resolve enumeration from enum map when contextual type")
    void shouldResolveEnumerationFromEnumMapWhenContextualType() throws Exception {
      // Given
      EnumerationDeserializer<IntegerEnum> deserializer = new EnumerationDeserializer<>();
      DeserializationContext context = mock(DeserializationContext.class);
      JavaType contextualType = mock(JavaType.class);
      when(context.getContextualType()).thenReturn(contextualType);
      when(contextualType.getRawClass()).thenAnswer(invocation -> IntegerEnum.class);

      // When
      ValueDeserializer<?> contextual =
          deserializer.createContextual(context, mock(BeanProperty.class));
      IntegerEnum result = deserialize(contextual, "1");

      // Then
      assertThat(result).isEqualTo(IntegerEnum.A);
    }
  }

  private IntegerEnum deserialize(ValueDeserializer<?> deserializer, String json) throws Exception {
    ObjectReader reader = jsonMapper.readerFor(integerEnumType);
    try (JsonParser parser = reader.createParser(json)) {
      parser.nextToken();
      DeserializationContext context = mock(DeserializationContext.class);
      @SuppressWarnings("unchecked")
      IntegerEnum result =
          ((ValueDeserializer<IntegerEnum>) deserializer).deserialize(parser, context);
      return result;
    }
  }

  private LongEnum deserializeLong(ValueDeserializer<?> deserializer, String json)
      throws Exception {
    JavaType longEnumType = jsonMapper.getTypeFactory().constructType(LongEnum.class);
    ObjectReader reader = jsonMapper.readerFor(longEnumType);
    try (JsonParser parser = reader.createParser(json)) {
      parser.nextToken();
      DeserializationContext context = mock(DeserializationContext.class);
      @SuppressWarnings("unchecked")
      LongEnum result = ((ValueDeserializer<LongEnum>) deserializer).deserialize(parser, context);
      return result;
    }
  }

  private static Map<Object, IntegerEnum> valueMap() {
    Map<Object, IntegerEnum> valueMap = new HashMap<>();
    for (IntegerEnum value : IntegerEnum.values()) {
      valueMap.put(value.getValue(), value);
      valueMap.put(String.valueOf(value.getValue()), value);
    }
    return valueMap;
  }

  private static Map<Object, LongEnum> longValueMap() {
    Map<Object, LongEnum> valueMap = new HashMap<>();
    for (LongEnum value : LongEnum.values()) {
      valueMap.put(value.getValue(), value);
      valueMap.put(String.valueOf(value.getValue()), value);
    }
    return valueMap;
  }
}
