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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import io.github.jinganix.webpb.runtime.model.BadRequest;
import io.github.jinganix.webpb.runtime.model.FooController;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
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

class WebpbRequestBodyAdviceTest {

  private MethodParameter getMethodParameter(WebpbRequestBodyAdvice advice) {
    Method method =
        ReflectionUtils.findMethod(FooController.class, "getFoo", FooRequest.class).orElse(null);
    assertNotNull(method);
    InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(advice, method);
    return handlerMethod.getMethodParameters()[0];
  }

  @Test
  void shouldSupportWebpbMessage() {
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    assertTrue(
        advice.supports(
            methodParameter, mock(Type.class), ObjectToStringHttpMessageConverter.class));
  }

  @Test
  void shouldReturnOriginBodyWhenRequestIsNull() {
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    FooRequest request = new FooRequest();
    FooRequest body =
        (FooRequest)
            advice.afterBodyRead(
                request,
                mock(HttpInputMessage.class),
                methodParameter,
                mock(Type.class),
                ObjectToStringHttpMessageConverter.class);
    assertEquals(request.getId(), body.getId());
  }

  @Test
  void shouldReturnOriginBodyWhenRequestWithoutParameters() {
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    BadRequest request = new BadRequest();
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
      assertEquals(request, body);
    }
  }

  @Test
  void shouldReturnOriginBodyWhenRequestWithoutWepebMeta() {
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    BadRequest request = new BadRequest();
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
      assertEquals(request, body);
    }
  }

  @Test
  void shouldReturnOriginBodyWhenJakartaRequest() {
    WebpbRequestBodyAdvice advice = new WebpbRequestBodyAdvice();
    MethodParameter methodParameter = getMethodParameter(advice);
    BadRequest request = new BadRequest();
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
      assertEquals(request, body);
    }
  }
}
