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

package io.github.jinganix.webpb.sample.backend;

import io.github.jinganix.webpb.runtime.mvc.WebpbHandlerMethodArgumentResolver;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestBodyAdvice;
import io.github.jinganix.webpb.runtime.reactive.WebpbClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configuration for beans. */
@EnableWebMvc
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  /**
   * {@link WebpbRequestBodyAdvice} bean.
   *
   * @return {@link WebpbRequestBodyAdvice}
   */
  @Bean
  public WebpbRequestBodyAdvice requestBodyAdvice() {
    return new WebpbRequestBodyAdvice();
  }

  /**
   * {@link WebpbClient} bean.
   *
   * @param port server listening port
   * @return {@link WebpbClient}
   */
  @Bean
  public WebpbClient webpbClient(@Value("${server.port}") int port) {
    return new WebpbClient(
        WebClient.builder()
            .baseUrl("http://localhost:" + port)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build());
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new WebpbHandlerMethodArgumentResolver());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("*");
  }
}
