/*
 * Copyright (c) 2020 jinganix@gmail.com, All Rights Reserved.
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
 */

package io.github.jinganix.webpb.runtime.mvc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import java.lang.reflect.Type;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/** Autowire request body properties from url path an query. */
@RestControllerAdvice
public class WebpbRequestBodyAdvice extends RequestBodyAdviceAdapter {

  private final ObjectMapper objectMapper;

  /** Construct an instance of {@link WebpbRequestBodyAdvice}. */
  public WebpbRequestBodyAdvice() {
    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * Construct an instance of {@link WebpbRequestBodyAdvice}.
   *
   * @param objectMapper {@link ObjectMapper}
   */
  public WebpbRequestBodyAdvice(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return WebpbMessage.class.isAssignableFrom(methodParameter.getParameterType());
  }

  @Override
  public Object afterBodyRead(
      Object body,
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    Object object = super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);

    Map<String, String> variableMap = VariablesResolver.getVariableMap();
    if (variableMap == null) {
      return object;
    }
    return WebpbUtils.updateMessage((WebpbMessage) object, variableMap);
  }
}
