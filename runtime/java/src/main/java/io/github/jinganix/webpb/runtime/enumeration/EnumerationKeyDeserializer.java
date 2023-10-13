/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
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
 *
 * https://github.com/jinganix/webpb
 */

package io.github.jinganix.webpb.runtime.enumeration;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualKeyDeserializer;
import java.util.HashMap;
import java.util.Map;

/** EnumerationKeyDeserializer. */
public class EnumerationKeyDeserializer extends KeyDeserializer
    implements ContextualKeyDeserializer {

  private final Map<Object, Object> valueMap = new HashMap<>();

  /** Constructor. */
  public EnumerationKeyDeserializer() {}

  /**
   * Deserialize an {@link Enumeration} Key.
   *
   * @param key key string
   * @param ctx {@link DeserializationContext}
   * @return {@link Enumeration}
   */
  @Override
  public Object deserializeKey(String key, DeserializationContext ctx) {
    return this.valueMap.get(key);
  }

  /**
   * createContextual.
   *
   * @param ctx {@link DeserializationContext}
   * @param property {@link BeanProperty}
   * @return {@link KeyDeserializer}
   */
  @Override
  public KeyDeserializer createContextual(DeserializationContext ctx, BeanProperty property) {
    Class<?> clazz = ctx.getContextualType().getKeyType().getRawClass();
    for (Enumeration<?> value : (Enumeration<?>[]) clazz.getEnumConstants()) {
      valueMap.put(String.valueOf(value.getValue()), value);
    }
    return this;
  }
}
