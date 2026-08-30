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
import io.github.jinganix.webpb.runtime.common.JacksonConfig;
import io.github.jinganix.webpb.runtime.support.WebpbRequestBodySupport;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

/**
 * WebFlux counterpart of {@link io.github.jinganix.webpb.runtime.mvc.WebpbRequestBodyAdvice}.
 *
 * <p>Registers as a custom JSON decoder so {@code @RequestBody WebpbMessage} parameters merge path
 * and query variables after JSON deserialization.
 */
public class WebpbReactiveRequestBodyAdvice extends JacksonJsonDecoder {

  /**
   * Construct a {@link WebpbReactiveRequestBodyAdvice} with the webpb transport {@link JsonMapper}.
   */
  public WebpbReactiveRequestBodyAdvice() {
    super((JsonMapper) JacksonConfig.createTransportObjectMapper());
  }

  @Override
  public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
    Class<?> clazz = elementType.toClass();
    return clazz != null
        && clazz != WebpbMessage.class
        && WebpbMessage.class.isAssignableFrom(clazz)
        && super.canDecode(elementType, mimeType);
  }

  @Override
  public Mono<Object> decodeToMono(
      Publisher<DataBuffer> input,
      ResolvableType elementType,
      MimeType mimeType,
      Map<String, Object> hints) {
    return super.decodeToMono(input, elementType, mimeType, hints).flatMap(this::mergeBodyAsync);
  }

  @Override
  public Flux<Object> decode(
      Publisher<DataBuffer> input,
      ResolvableType elementType,
      MimeType mimeType,
      Map<String, Object> hints) {
    return super.decode(input, elementType, mimeType, hints).flatMap(this::mergeBodyAsync);
  }

  private Mono<Object> mergeBodyAsync(Object body) {
    return Mono.deferContextual(
        contextView ->
            Mono.just(
                WebpbRequestBodySupport.mergeBody(
                    body, ReactiveVariablesResolver.getVariableMap(contextView))));
  }
}
