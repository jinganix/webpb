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
import static org.mockito.Mockito.when;

import io.github.jinganix.webpb.runtime.model.FooController;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

@DisplayName("WebpbHandlerMethodArgumentResolver")
class WebpbHandlerMethodArgumentResolverTest {

  private MethodParameter webpbParameter() throws Exception {
    Method method =
        ReflectionUtils.findMethod(FooController.class, "getFoo", FooRequest.class).orElseThrow();
    return new InvocableHandlerMethod(new FooController(), method).getMethodParameters()[0];
  }

  @Test
  @DisplayName("should support webpb message parameters without request body annotation")
  void shouldSupportWebpbMessageParametersWithoutRequestBodyAnnotation() throws Exception {
    // Given
    WebpbHandlerMethodArgumentResolver resolver = new WebpbHandlerMethodArgumentResolver();

    // When / Then
    assertThat(resolver.supportsParameter(webpbParameter())).isTrue();
  }

  @Test
  @DisplayName("should not support request body parameters")
  void shouldNotSupportRequestBodyParameters() {
    // Given
    WebpbHandlerMethodArgumentResolver resolver = new WebpbHandlerMethodArgumentResolver();
    MethodParameter parameter = mock(MethodParameter.class);
    when(parameter.getParameterType()).thenReturn((Class) FooRequest.class);
    when(parameter.hasParameterAnnotation(RequestBody.class)).thenReturn(true);

    // When / Then
    assertThat(resolver.supportsParameter(parameter)).isFalse();
  }

  @Test
  @DisplayName("should resolve argument from uri template and query parameters")
  void shouldResolveArgumentFromUriTemplateAndQueryParameters() throws Exception {
    // Given
    WebpbHandlerMethodArgumentResolver resolver = new WebpbHandlerMethodArgumentResolver();
    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setAttribute(
        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("id", "42"));
    servletRequest.setParameter("pagination", "true");
    NativeWebRequest webRequest = new ServletWebRequest(servletRequest);

    // When
    Object argument = resolver.resolveArgument(webpbParameter(), null, webRequest, null);

    // Then
    assertThat(argument).isInstanceOf(FooRequest.class);
    assertThat(((FooRequest) argument).getId()).isEqualTo(42);
  }

  @Test
  @DisplayName("should add binding result when binder factory is provided")
  void shouldAddBindingResultWhenBinderFactoryIsProvided() throws Exception {
    // Given
    WebpbHandlerMethodArgumentResolver resolver = new WebpbHandlerMethodArgumentResolver();
    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setParameter("id", "7");
    NativeWebRequest webRequest = new ServletWebRequest(servletRequest);
    ModelAndViewContainer mavContainer = new ModelAndViewContainer();
    WebDataBinderFactory binderFactory =
        (request, target, objectName) -> new WebDataBinder(target, objectName);

    // When
    Object argument =
        resolver.resolveArgument(webpbParameter(), mavContainer, webRequest, binderFactory);

    // Then
    assertThat(argument).isInstanceOf(FooRequest.class);
    assertThat(mavContainer.getModel()).isNotEmpty();
  }
}
