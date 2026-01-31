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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jinganix.webpb.runtime.common.InQuery;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;

/** JsonTransportMapper. */
public class JsonTransportMapper implements TransportMapper {

  private final ObjectMapper objectMapper;

  /** Constructor. */
  public JsonTransportMapper() {
    this(JsonMapper.builder());
  }

  /**
   * Constructor.
   *
   * @param builder {@link JsonMapper.Builder}
   */
  public JsonTransportMapper(JsonMapper.Builder builder) {
    this.objectMapper =
        builder
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .changeDefaultPropertyInclusion(
                incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .annotationIntrospector(
                new JacksonAnnotationIntrospector() {
                  @Override
                  public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
                    return super.hasIgnoreMarker(config, m) || m.hasAnnotation(InQuery.class);
                  }
                })
            .build();
  }

  /**
   * Write value as string.
   *
   * @param value {@link Object}
   * @return string
   */
  @Override
  public String writeValue(Object value) {
    return objectMapper.writeValueAsString(value);
  }

  /**
   * Read value as object.
   *
   * @param src byte array
   * @param valueType {@link Class}
   * @param <T> type of T
   * @return {@link T}
   */
  @Override
  public <T> T readValue(byte[] src, Class<T> valueType) {
    return objectMapper.readValue(src, valueType);
  }
}
