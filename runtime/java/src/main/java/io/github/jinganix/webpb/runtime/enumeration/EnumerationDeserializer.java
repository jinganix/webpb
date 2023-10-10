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

package io.github.jinganix.webpb.runtime.enumeration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * EnumerationSerializer.
 *
 * @param <E> enum type
 */
public class EnumerationDeserializer<E extends Enumeration<?>> extends JsonDeserializer<E>
    implements ContextualDeserializer {

  private final Map<Object, E> valueMap = new HashMap<>();

  /** Constructor. */
  public EnumerationDeserializer() {}

  /**
   * Deserialize an {@link Enumeration} value.
   *
   * @param p {@link JsonParser}
   * @param ctx {@link DeserializationContext}
   * @return instance of {@link Enumeration}
   * @throws IOException throw exception when failed
   */
  @Override
  public E deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
    if (p.hasToken(JsonToken.VALUE_STRING)) {
      return valueMap.get(p.getText());
    }
    if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
      return valueMap.get(p.getIntValue());
    }
    return null;
  }

  /**
   * createContextual.
   *
   * @param ctx {@link DeserializationContext}
   * @param property {@link BeanProperty}
   * @return {@link JsonDeserializer}
   */
  @SuppressWarnings("unchecked")
  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
    Class<E> clazz = (Class<E>) ctx.getContextualType().getRawClass();
    for (E value : clazz.getEnumConstants()) {
      valueMap.put(value.getValue(), value);
      valueMap.put(String.valueOf(value.getValue()), value);
    }
    return this;
  }
}
