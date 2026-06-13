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

import io.github.jinganix.webpb.sample.proto.options.AliasPayloadPb;
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
import io.github.jinganix.webpb.sample.proto.options.StatusPb;
import io.github.jinganix.webpb.sample.proto.options.ValidationRequest;
import io.github.jinganix.webpb.sample.proto.options.ValidationResponse;
import org.springframework.stereotype.Service;

/** Options showcase service. */
@Service
public class OptionsService {

  /** Creates the options service. */
  public OptionsService() {}

  /**
   * Echoes the HTTP route request name.
   *
   * @param request HTTP route request
   * @return echoed name
   */
  public HttpRouteResponse httpRoute(HttpRouteRequest request) {
    return new HttpRouteResponse("echo: " + request.getName());
  }

  /**
   * Returns resolved context paths for the request.
   *
   * @param request context path request
   * @return resolved paths
   */
  public ContextPathResponse contextPath(ContextPathRequest request) {
    return new ContextPathResponse("/api/options/context", "/options/context");
  }

  /**
   * Echoes the query parameter tag.
   *
   * @param request query parameter request
   * @return echoed tag
   */
  public QueryParamResponse queryParam(QueryParamRequest request) {
    return new QueryParamResponse(request.getTag());
  }

  /**
   * Returns a successful validation response.
   *
   * @param request validation request
   * @return validation result
   */
  public ValidationResponse validation(ValidationRequest request) {
    return new ValidationResponse(true);
  }

  /**
   * Echoes the int64 identifier.
   *
   * @param request int64 string request
   * @return echoed identifier
   */
  public Int64StringResponse int64String(Int64StringRequest request) {
    return new Int64StringResponse(request.getId());
  }

  /**
   * Wraps the request text in an auto-aliased payload.
   *
   * @param request auto alias request
   * @return aliased payload
   */
  public AutoAliasResponse autoAlias(AutoAliasRequest request) {
    return new AutoAliasResponse(new AliasPayloadPb(request.getText()));
  }

  /**
   * Echoes the field-aliased title.
   *
   * @param request field alias request
   * @return echoed title
   */
  public FieldAliasResponse fieldAlias(FieldAliasRequest request) {
    return new FieldAliasResponse(request.getTitle());
  }

  /**
   * Returns the request status, defaulting to unknown when absent.
   *
   * @param request enum string request
   * @return resolved status
   */
  public EnumStringResponse enumString(EnumStringRequest request) {
    StatusPb status = request.getStatus() == null ? StatusPb.UNKNOWN : request.getStatus();
    return new EnumStringResponse(status);
  }
}
