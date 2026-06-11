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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jinganix.webpb.runtime.model.FooRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("XmlTransportMapper")
class XmlTransportMapperTest {

  @Test
  @DisplayName("should write and read xml values when mapper is configured")
  void shouldWriteAndReadXmlValuesWhenMapperIsConfigured() {
    // Given
    XmlTransportMapper mapper = new XmlTransportMapper();
    FooRequest request = new FooRequest().setData("hello");

    // When
    String xml = mapper.writeValue(request);
    FooRequest parsed = mapper.readValue(xml.getBytes(), FooRequest.class);

    // Then
    assertThat(parsed.getData()).isEqualTo("hello");
  }
}
