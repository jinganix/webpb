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

package io.github.jinganix.webpb.runtime.webflux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.jinganix.webpb.runtime.model.FooController;
import io.github.jinganix.webpb.runtime.model.FooRequest;
import io.github.jinganix.webpb.runtime.model.ValidatedFooController;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.servlet.HandlerMapping;

@DisplayName("WebpbReactiveHandlerMethodArgumentResolver")
class WebpbReactiveHandlerMethodArgumentResolverTest {

  private MethodParameter webpbParameter() throws Exception {
    Method method =
        ReflectionUtils.findMethod(FooController.class, "getFoo", FooRequest.class).orElseThrow();
    return new InvocableHandlerMethod(new FooController(), method).getMethodParameters()[0];
  }

  @Test
  @DisplayName("should support webpb message parameters without request body annotation")
  void shouldSupportWebpbMessageParametersWithoutRequestBodyAnnotation() throws Exception {
    // Given
    WebpbReactiveHandlerMethodArgumentResolver resolver =
        new WebpbReactiveHandlerMethodArgumentResolver();

    // When / Then
    assertThat(resolver.supportsParameter(webpbParameter())).isTrue();
  }

  @Test
  @DisplayName("should not support request body parameters")
  void shouldNotSupportRequestBodyParameters() {
    // Given
    WebpbReactiveHandlerMethodArgumentResolver resolver =
        new WebpbReactiveHandlerMethodArgumentResolver();
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
    WebpbReactiveHandlerMethodArgumentResolver resolver =
        new WebpbReactiveHandlerMethodArgumentResolver();
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/domain/42/action?pagination=true").build());
    exchange
        .getAttributes()
        .put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("id", "42"));

    // When
    FooRequest argument =
        (FooRequest)
            resolver.resolveArgument(webpbParameter(), new BindingContext(), exchange).block();

    // Then
    assertThat(argument).isNotNull();
    assertThat(argument.getId()).isEqualTo(42);
    assertThat(argument.isPagination()).isTrue();
  }

  @Test
  @DisplayName("should validate argument when parameter has valid annotation")
  void shouldValidateArgumentWhenParameterHasValidAnnotation() throws Exception {
    // Given
    WebpbReactiveHandlerMethodArgumentResolver resolver =
        new WebpbReactiveHandlerMethodArgumentResolver();
    Method method =
        ReflectionUtils.findMethod(ValidatedFooController.class, "getFoo", FooRequest.class)
            .orElseThrow();
    MethodParameter parameter =
        new InvocableHandlerMethod(new ValidatedFooController(), method).getMethodParameters()[0];
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/domain/42/action?pagination=true").build());
    exchange
        .getAttributes()
        .put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("id", "42"));

    // When
    FooRequest argument =
        (FooRequest) resolver.resolveArgument(parameter, new BindingContext(), exchange).block();

    // Then
    assertThat(argument).isNotNull();
    assertThat(argument.getId()).isEqualTo(42);
  }
}
