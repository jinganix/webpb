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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooController;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.MockedStatic;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.ObjectToStringHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

@DisplayName("WebpbRequestBodyAdvice")
class WebpbRequestBodyAdviceTest {

  private MethodParameter getMethodParameter(WebpbRequestBodyAdvice advice) {
    Method method =
        ReflectionUtils.findMethod(FooController.class, "getFoo", FooRequest.class).orElse(null);
    assertThat(method).isNotNull();
    InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(advice, method);
    return handlerMethod.getMethodParameters()[0];
  }

  @Test
  @DisplayName("should support webpb message parameters")
  void shouldSupportWebpbMessageParameters() {
    // Given
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);

    // When / Then
    assertThat(
            advice.supports(
                methodParameter, mock(Type.class), ObjectToStringHttpMessageConverter.class))
        .isTrue();
  }

  @Test
  @DisplayName("should return original body when request context is null")
  void shouldReturnOriginalBodyWhenRequestContextIsNull() {
    // Given
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    FooRequest request = new FooRequest();

    // When
    FooRequest body =
        (FooRequest)
            advice.afterBodyRead(
                request,
                mock(HttpInputMessage.class),
                methodParameter,
                mock(Type.class),
                ObjectToStringHttpMessageConverter.class);

    // Then
    assertThat(body.getId()).isEqualTo(request.getId());
  }

  @Test
  @DisplayName("should return original body when request has no parameters")
  void shouldReturnOriginalBodyWhenRequestHasNoParameters() {
    // Given
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    BadRequest request = new BadRequest();

    // When
    try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
      MockHttpServletRequest servletRequest = new MockHttpServletRequest();
      ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
      holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

      Object body =
          advice.afterBodyRead(
              request,
              mock(HttpInputMessage.class),
              methodParameter,
              mock(Type.class),
              ObjectToStringHttpMessageConverter.class);

      // Then
      assertThat(body).isEqualTo(request);
    }
  }

  @Test
  @DisplayName("should return original body when message has no webpb meta")
  void shouldReturnOriginalBodyWhenMessageHasNoWebpbMeta() {
    // Given
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    BadRequest request = new BadRequest();

    // When
    try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
      MockHttpServletRequest servletRequest = new MockHttpServletRequest();
      servletRequest.setParameter("id", "12345678");
      servletRequest.setAttribute(
          HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());
      ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
      holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

      Object body =
          advice.afterBodyRead(
              request,
              mock(HttpInputMessage.class),
              methodParameter,
              mock(Type.class),
              ObjectToStringHttpMessageConverter.class);

      // Then
      assertThat(body).isEqualTo(request);
    }
  }

  @Test
  @DisplayName("should merge variables into body when request has webpb meta")
  void shouldMergeVariablesIntoBodyWhenRequestHasWebpbMeta() {
    // Given
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    FooRequest request = new FooRequest();

    // When
    try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
      MockHttpServletRequest servletRequest = new MockHttpServletRequest();
      servletRequest.setParameter("id", "42");
      servletRequest.setAttribute(
          HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());
      ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
      holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

      FooRequest body =
          (FooRequest)
              advice.afterBodyRead(
                  request,
                  mock(HttpInputMessage.class),
                  methodParameter,
                  mock(Type.class),
                  ObjectToStringHttpMessageConverter.class);

      // Then
      assertThat(body.getId()).isEqualTo(42);
    }
  }

  @Test
  @DisplayName("should return original body when servlet request has no parameters")
  void shouldReturnOriginalBodyWhenServletRequestHasNoParameters() {
    // Given
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    BadRequest request = new BadRequest();

    // When
    try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
      MockHttpServletRequest servletRequest = new MockHttpServletRequest();
      ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
      holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

      Object body =
          advice.afterBodyRead(
              request,
              mock(HttpInputMessage.class),
              methodParameter,
              mock(Type.class),
              ObjectToStringHttpMessageConverter.class);

      // Then
      assertThat(body).isEqualTo(request);
    }
  }
}
