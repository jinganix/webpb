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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

/**
 * Optional Spring configuration for WebFlux server support.
 *
 * <p>Registers {@link WebpbReactiveHandlerMethodArgumentResolver}, {@link
 * WebpbReactiveRequestBodyAdvice}, and {@link WebpbExchangeWebFilter}.
 */
@Configuration
public class WebpbWebFluxConfiguration implements WebFluxConfigurer {

  /** Construct a {@link WebpbWebFluxConfiguration}. */
  public WebpbWebFluxConfiguration() {}

  /**
   * {@link WebpbExchangeWebFilter} bean.
   *
   * @return {@link WebpbExchangeWebFilter}
   */
  @Bean
  public WebpbExchangeWebFilter webpbExchangeWebFilter() {
    return new WebpbExchangeWebFilter();
  }

  @Override
  public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
    configurer.addCustomResolver(new WebpbReactiveHandlerMethodArgumentResolver());
  }

  @Override
  public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    configurer.customCodecs().register(new WebpbReactiveRequestBodyAdvice());
  }
}
