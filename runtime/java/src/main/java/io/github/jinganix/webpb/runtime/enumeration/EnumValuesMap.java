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
import java.util.concurrent.ConcurrentHashMap;

/** Cached lookup from wire values to {@link Enumeration} constants. */
public final class EnumValuesMap {

  private static final Map<Class<?>, Map<Object, Enumeration<?>>> valuesMap =
      new ConcurrentHashMap<>();

  private EnumValuesMap() {}

  @SuppressWarnings("unchecked")
  private static Map<Object, Enumeration<?>> createValueMap(Class<?> clazz) {
    if (!Enumeration.class.isAssignableFrom(clazz)) {
      return null;
    }
    Map<Object, Enumeration<?>> valueMap = new HashMap<>();
    Class<Enumeration<?>> enumClass = (Class<Enumeration<?>>) clazz;
    for (Enumeration<?> value : enumClass.getEnumConstants()) {
      valueMap.put(value.getValue(), value);
      valueMap.put(String.valueOf(value.getValue()), value);
      valueMap.put(String.valueOf(value), value);
    }
    return valueMap;
  }

  /**
   * Return a cached lookup from wire values to {@link Enumeration} constants.
   *
   * @param clazz enumeration class
   * @return value map, or {@code null} when {@code clazz} is not an {@link Enumeration}
   */
  public static Map<Object, Enumeration<?>> getValueMap(Class<?> clazz) {
    return valuesMap.computeIfAbsent(clazz, EnumValuesMap::createValueMap);
  }
}
