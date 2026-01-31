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

import java.util.HashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * EnumerationSerializer.
 *
 * @param <E> enum type
 */
public class EnumerationDeserializer<E extends Enumeration<?>> extends ValueDeserializer<E> {

  private Map<Object, E> valueMap = new HashMap<>();

  /** Constructor. */
  public EnumerationDeserializer() {}

  /**
   * Deserialize an {@link Enumeration} value.
   *
   * @param p {@link JsonParser}
   * @param ctx {@link DeserializationContext}
   * @return instance of {@link Enumeration}
   * @throws JacksonException throw exception when failed
   */
  @Override
  public E deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
    if (p.hasToken(JsonToken.VALUE_STRING)) {
      return valueMap.get(p.getString());
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
   */
  @Override
  @SuppressWarnings("unchecked")
  public ValueDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
    Class<E> clazz = (Class<E>) ctx.getContextualType().getRawClass();
    this.valueMap = (Map<Object, E>) EnumValuesMap.getValueMap(clazz);
    return this;
  }
}
