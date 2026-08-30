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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@DisplayName("WebpbWebFluxConfiguration")
class WebpbWebFluxConfigurationTest {

  @Test
  @DisplayName("should register webflux components")
  void shouldRegisterWebfluxComponents() {
    // Given
    WebpbWebFluxConfiguration configuration = new WebpbWebFluxConfiguration();

    // When
    ArgumentResolverConfigurer resolverConfigurer = new ArgumentResolverConfigurer();
    configuration.configureArgumentResolvers(resolverConfigurer);
    DefaultServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
    configuration.configureHttpMessageCodecs(codecConfigurer);

    // Then
    assertThat(configuration.webpbExchangeWebFilter()).isNotNull();
  }
}
