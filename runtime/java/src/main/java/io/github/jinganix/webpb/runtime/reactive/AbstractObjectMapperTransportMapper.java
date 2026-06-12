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

package io.github.jinganix.webpb.runtime.reactive;

import tools.jackson.databind.ObjectMapper;

/** Transport mapper backed by a Jackson {@link ObjectMapper}. */
abstract class AbstractObjectMapperTransportMapper implements TransportMapper {

  private final ObjectMapper objectMapper;

  /**
   * Constructor.
   *
   * @param objectMapper Jackson mapper for transport serialization
   */
  protected AbstractObjectMapperTransportMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String writeValue(Object value) {
    return objectMapper.writeValueAsString(value);
  }

  @Override
  public <T> T readValue(byte[] src, Class<T> valueType) {
    return objectMapper.readValue(src, valueType);
  }
}
