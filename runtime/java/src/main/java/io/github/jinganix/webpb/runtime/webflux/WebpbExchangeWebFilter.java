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

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Propagates the current {@link ServerWebExchange} through the Reactor context so request body
 * decoders can merge path and query variables after JSON deserialization.
 */
public class WebpbExchangeWebFilter implements WebFilter {

  /** Reactor context key for the current {@link ServerWebExchange}. */
  public static final String EXCHANGE_CONTEXT_KEY =
      "io.github.jinganix.webpb.runtime.webflux.WebpbExchangeWebFilter.EXCHANGE";

  /** Construct a {@link WebpbExchangeWebFilter}. */
  public WebpbExchangeWebFilter() {}

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return chain.filter(exchange).contextWrite(context -> context.put(EXCHANGE_CONTEXT_KEY, exchange));
  }
}
