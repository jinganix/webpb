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

package io.github.jinganix.webpb.sample.backend;

import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;
import io.github.jinganix.webpb.sample.proto.store.StoreGreetingRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreGreetingResponse;
import io.github.jinganix.webpb.sample.proto.store.StoreListRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreListResponse;
import io.github.jinganix.webpb.sample.proto.store.StoreVisitRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreVisitResponse;
<#if java17>import jakarta.validation.Valid;
<#else>import javax.validation.Valid;
</#if>import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Store controller. */
@RestController
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;

  /**
   * Request a store data.
   *
   * @param request {@link StoreVisitRequest}
   * @return {@link StoreVisitResponse}
   */
  @WebpbRequestMapping
  public StoreVisitResponse getStore(@Valid @RequestBody StoreVisitRequest request) {
    return storeService.getStore(request);
  }

  /**
   * Request a list of stores.
   *
   * @param request {@link StoreListRequest}
   * @return {@link StoreListResponse}
   */
  @WebpbRequestMapping
  public StoreListResponse getStores(@Valid StoreListRequest request) {
    return storeService.getStores(request);
  }

  /**
   * Request a greeting message.
   *
   * @param request {@link StoreGreetingRequest}
   * @return {@link StoreGreetingResponse}
   */
  @WebpbRequestMapping
  public StoreGreetingResponse greeting(@Valid @RequestBody StoreGreetingRequest request) {
    return storeService.greeting(request);
  }
}
