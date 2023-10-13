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

package io.github.jinganix.webpb.runtime.reactive;

/** TransportMapper interface. */
public interface TransportMapper {

  /**
   * Write value as string.
   *
   * @param value {@link Object}
   * @return string
   */
  String writeValue(Object value);

  /**
   * Read value as object.
   *
   * @param src byte array
   * @param valueType {@link Class}
   * @param <T> type of T
   * @return {@link T}
   */
  <T> T readValue(byte[] src, Class<T> valueType);
}
