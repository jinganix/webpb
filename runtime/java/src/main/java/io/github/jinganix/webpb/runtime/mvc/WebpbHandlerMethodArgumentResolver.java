/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import static io.github.jinganix.webpb.runtime.mvc.WebpbRequestUtils.mergeVariables;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import java.util.Collections;
import java.util.Map;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

/** Resolve {@link WebpbMessage} argument without {@link RequestBody} annotation. */
public class WebpbHandlerMethodArgumentResolver
    extends AbstractMessageConverterMethodArgumentResolver {

  /** Construct a {@link WebpbHandlerMethodArgumentResolver}. */
  public WebpbHandlerMethodArgumentResolver() {
    super(Collections.singletonList(new ByteArrayHttpMessageConverter()));
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return WebpbMessage.class.isAssignableFrom(parameter.getParameterType())
        && !parameter.hasParameterAnnotation(RequestBody.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory)
      throws Exception {
    Object object = parameter.getParameterType().getDeclaredConstructor().newInstance();

    @SuppressWarnings("unchecked")
    Map<String, String> attributes =
        (Map<String, String>)
            webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, 0);
    Map<String, String[]> parameters = webRequest.getParameterMap();
    Map<String, String> variablesMap = mergeVariables(attributes, parameters);
    Object arg = WebpbUtils.updateMessage((WebpbMessage) object, variablesMap);

    if (binderFactory != null) {
      String name = Conventions.getVariableNameForParameter(parameter);
      WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
      if (arg != null) {
        validateIfApplicable(binder, parameter);
        if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
          throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
        }
      }
      if (mavContainer != null) {
        mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
      }
    }
    return adaptArgumentIfNecessary(arg, parameter);
  }
}
