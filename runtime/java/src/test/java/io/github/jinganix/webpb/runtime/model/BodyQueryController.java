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

package io.github.jinganix.webpb.runtime.model;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Controller for reactive body and query binding tests. */
@RestController
public class BodyQueryController {

  private BodyQueryRequest lastRequest;

  /**
   * Create a post with body and query parameters.
   *
   * @param request {@link BodyQueryRequest}
   * @return {@link BodyQueryRequest}
   */
  @PostMapping("/posts/{id}")
  public Mono<BodyQueryRequest> create(@RequestBody BodyQueryRequest request) {
    lastRequest = request;
    return Mono.just(request);
  }

  /**
   * Return the last handled request.
   *
   * @return last request
   */
  BodyQueryRequest lastRequest() {
    return lastRequest;
  }
}
