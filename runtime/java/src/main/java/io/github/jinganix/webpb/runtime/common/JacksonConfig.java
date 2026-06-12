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

package io.github.jinganix.webpb.runtime.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

/** Shared Jackson {@link ObjectMapper} configuration for webpb runtime. */
public final class JacksonConfig {

  private static final JacksonAnnotationIntrospector IN_QUERY_INTROSPECTOR =
      new JacksonAnnotationIntrospector() {
        @Override
        public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember member) {
          return super.hasIgnoreMarker(config, member) || member.hasAnnotation(InQuery.class);
        }
      };

  private JacksonConfig() {}

  /**
   * Configure a {@link JsonMapper.Builder} for transport serialization.
   *
   * @param builder {@link JsonMapper.Builder}
   * @return configured builder
   */
  public static JsonMapper.Builder configureTransport(JsonMapper.Builder builder) {
    return builder
        .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .changeDefaultPropertyInclusion(
            incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
        .annotationIntrospector(IN_QUERY_INTROSPECTOR);
  }

  /**
   * Configure an {@link XmlMapper.Builder} for transport serialization.
   *
   * @param builder {@link XmlMapper.Builder}
   * @return configured builder
   */
  public static XmlMapper.Builder configureTransport(XmlMapper.Builder builder) {
    return builder
        .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .changeDefaultPropertyInclusion(
            incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
        .annotationIntrospector(IN_QUERY_INTROSPECTOR);
  }

  /**
   * Create an {@link ObjectMapper} for transport serialization.
   *
   * @return {@link ObjectMapper}
   */
  public static ObjectMapper createTransportObjectMapper() {
    return configureTransport(JsonMapper.builder()).build();
  }

  /**
   * Create an {@link ObjectMapper} for URL path and query formatting.
   *
   * @return {@link ObjectMapper}
   */
  public static ObjectMapper createUrlObjectMapper() {
    return JsonMapper.builder().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).build();
  }
}
