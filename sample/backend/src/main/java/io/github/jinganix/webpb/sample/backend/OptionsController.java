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

import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;
import io.github.jinganix.webpb.sample.proto.options.AutoAliasRequest;
import io.github.jinganix.webpb.sample.proto.options.AutoAliasResponse;
import io.github.jinganix.webpb.sample.proto.options.ContextPathRequest;
import io.github.jinganix.webpb.sample.proto.options.ContextPathResponse;
import io.github.jinganix.webpb.sample.proto.options.EnumStringRequest;
import io.github.jinganix.webpb.sample.proto.options.EnumStringResponse;
import io.github.jinganix.webpb.sample.proto.options.FieldAliasRequest;
import io.github.jinganix.webpb.sample.proto.options.FieldAliasResponse;
import io.github.jinganix.webpb.sample.proto.options.HttpRouteRequest;
import io.github.jinganix.webpb.sample.proto.options.HttpRouteResponse;
import io.github.jinganix.webpb.sample.proto.options.Int64StringRequest;
import io.github.jinganix.webpb.sample.proto.options.Int64StringResponse;
import io.github.jinganix.webpb.sample.proto.options.QueryParamRequest;
import io.github.jinganix.webpb.sample.proto.options.QueryParamResponse;
import io.github.jinganix.webpb.sample.proto.options.ValidationRequest;
import io.github.jinganix.webpb.sample.proto.options.ValidationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Options showcase controller. */
@RestController
public class OptionsController {

  private final OptionsService optionsService;

  /**
   * Creates the options controller.
   *
   * @param optionsService options service
   */
  public OptionsController(OptionsService optionsService) {
    this.optionsService = optionsService;
  }

  /**
   * Handles the HTTP route option showcase.
   *
   * @param request HTTP route request
   * @return echoed name
   */
  @WebpbRequestMapping
  public HttpRouteResponse httpRoute(@Valid @RequestBody HttpRouteRequest request) {
    return optionsService.httpRoute(request);
  }

  /**
   * Handles the context path option showcase.
   *
   * @param request context path request
   * @return resolved paths
   */
  @WebpbRequestMapping
  public ContextPathResponse contextPath(@Valid ContextPathRequest request) {
    return optionsService.contextPath(request);
  }

  /**
   * Handles the query parameter option showcase.
   *
   * @param request query parameter request
   * @return echoed tag
   */
  @WebpbRequestMapping
  public QueryParamResponse queryParam(@Valid QueryParamRequest request) {
    return optionsService.queryParam(request);
  }

  /**
   * Handles the validation option showcase.
   *
   * @param request validation request
   * @return validation result
   */
  @WebpbRequestMapping
  public ValidationResponse validation(@Valid @RequestBody ValidationRequest request) {
    return optionsService.validation(request);
  }

  /**
   * Handles the int64 string option showcase.
   *
   * @param request int64 string request
   * @return echoed identifier
   */
  @WebpbRequestMapping
  public Int64StringResponse int64String(@Valid @RequestBody Int64StringRequest request) {
    return optionsService.int64String(request);
  }

  /**
   * Handles the auto alias option showcase.
   *
   * @param request auto alias request
   * @return aliased payload
   */
  @WebpbRequestMapping
  public AutoAliasResponse autoAlias(@Valid @RequestBody AutoAliasRequest request) {
    return optionsService.autoAlias(request);
  }

  /**
   * Handles the field alias option showcase.
   *
   * @param request field alias request
   * @return echoed title
   */
  @WebpbRequestMapping
  public FieldAliasResponse fieldAlias(@Valid @RequestBody FieldAliasRequest request) {
    return optionsService.fieldAlias(request);
  }

  /**
   * Handles the enum string option showcase.
   *
   * @param request enum string request
   * @return resolved status
   */
  @WebpbRequestMapping
  public EnumStringResponse enumString(@Valid @RequestBody EnumStringRequest request) {
    return optionsService.enumString(request);
  }
}
