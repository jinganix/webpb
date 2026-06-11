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

package io.github.jinganix.webpb.runtime.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

@DisplayName("VariablesResolver")
class VariablesResolverTest {

  @Test
  @DisplayName("should return merged variables when servlet request is available")
  void shouldReturnMergedVariablesWhenServletRequestIsAvailable() {
    // Given
    try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
      MockHttpServletRequest servletRequest = new MockHttpServletRequest();
      servletRequest.setParameter("id", "123");
      servletRequest.setAttribute(
          HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("name", "foo"));
      holder
          .when(RequestContextHolder::getRequestAttributes)
          .thenReturn(new ServletRequestAttributes(servletRequest));

      // When
      Map<String, String> variables = VariablesResolver.getVariableMap();

      // Then
      assertThat(variables).containsEntry("id", "123").containsEntry("name", "foo");
    }
  }

  @Test
  @DisplayName("should return empty map when request attributes are missing")
  void shouldReturnEmptyMapWhenRequestAttributesAreMissing() {
    // Given
    try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
      holder.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

      // When / Then
      assertThat(VariablesResolver.getVariableMap()).isEmpty();
    }
  }
}
