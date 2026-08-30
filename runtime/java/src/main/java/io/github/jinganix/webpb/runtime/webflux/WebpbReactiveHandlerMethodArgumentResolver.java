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

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import java.lang.annotation.Annotation;
import java.util.Map;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.support.WebExchangeDataBinder;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Resolve {@link WebpbMessage} argument without {@link RequestBody} annotation on WebFlux. */
public class WebpbReactiveHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  /** Construct a {@link WebpbReactiveHandlerMethodArgumentResolver}. */
  public WebpbReactiveHandlerMethodArgumentResolver() {}

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return WebpbMessage.class.isAssignableFrom(parameter.getParameterType())
        && !parameter.hasParameterAnnotation(RequestBody.class);
  }

  @Override
  public Mono<Object> resolveArgument(
      MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    return Mono.fromCallable(
            () -> {
              Object object = parameter.getParameterType().getDeclaredConstructor().newInstance();
              Map<String, String> variablesMap = ReactiveVariablesResolver.getVariableMap(exchange);
              return WebpbUtils.updateMessage((WebpbMessage) object, variablesMap);
            })
        .flatMap(
            arg -> {
              Object[] validationHints = extractValidationHints(parameter);
              if (validationHints == null) {
                return Mono.just(arg);
              }
              String name = Conventions.getVariableNameForParameter(parameter);
              WebExchangeDataBinder binder = bindingContext.createDataBinder(exchange, arg, name);
              binder.validate(validationHints);
              if (binder.getBindingResult().hasErrors()) {
                return Mono.error(
                    new WebExchangeBindException(parameter, binder.getBindingResult()));
              }
              return Mono.just(arg);
            });
  }

  private Object[] extractValidationHints(MethodParameter parameter) {
    for (Annotation annotation : parameter.getParameterAnnotations()) {
      Object[] hints = ValidationAnnotationUtils.determineValidationHints(annotation);
      if (hints != null) {
        return hints;
      }
    }
    return null;
  }
}
