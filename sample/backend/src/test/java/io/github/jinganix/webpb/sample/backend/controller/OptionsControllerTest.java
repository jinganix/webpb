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

package io.github.jinganix.webpb.sample.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import io.github.jinganix.webpb.sample.proto.options.AutoAliasRequest;
import io.github.jinganix.webpb.sample.proto.options.EnumStringRequest;
import io.github.jinganix.webpb.sample.proto.options.FieldAliasRequest;
import io.github.jinganix.webpb.sample.proto.options.HttpRouteRequest;
import io.github.jinganix.webpb.sample.proto.options.Int64StringRequest;
import io.github.jinganix.webpb.sample.proto.options.QueryParamRequest;
import io.github.jinganix.webpb.sample.proto.options.StatusPb;
import io.github.jinganix.webpb.sample.proto.options.ValidationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@DisplayName("OptionsController")
class OptionsControllerTest {

  @Autowired private MockMvc mvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockHttpServletRequestBuilder request(WebpbMessage message) {
    return MockMvcRequestBuilders.request(
            HttpMethod.valueOf(message.webpbMeta().getMethod()), WebpbUtils.formatUrl(message))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(message));
  }

  @Test
  @DisplayName("should echo name when http route is requested")
  void shouldEchoNameWhenHttpRouteIsRequested() throws Exception {
    // When / Then
    mvc.perform(request(new HttpRouteRequest("webpb")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.echo", is("echo: webpb")));
  }

  @Test
  @DisplayName("should return resolved paths when context path is requested")
  void shouldReturnResolvedPathsWhenContextPathIsRequested() throws Exception {
    // When / Then
    mvc.perform(MockMvcRequestBuilders.get("/options/context"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clientPath", is("/api/options/context")))
        .andExpect(jsonPath("$.serverPath", is("/options/context")));
  }

  @Test
  @DisplayName("should echo tag when query param is provided")
  void shouldEchoTagWhenQueryParamIsProvided() throws Exception {
    // When / Then
    mvc.perform(request(new QueryParamRequest("demo")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tag", is("demo")));
  }

  @Test
  @DisplayName("should return bad request when validation code is blank")
  void shouldReturnBadRequestWhenValidationCodeIsBlank() throws Exception {
    // When / Then
    mvc.perform(request(new ValidationRequest("")).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.code", is("must not be blank")));
  }

  @Test
  @DisplayName("should return ok when validation code is provided")
  void shouldReturnOkWhenValidationCodeIsProvided() throws Exception {
    // When / Then
    mvc.perform(request(new ValidationRequest("demo")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok", is(true)));
  }

  @Test
  @DisplayName("should echo int64 id when int64 string is requested")
  void shouldEchoInt64IdWhenInt64StringIsRequested() throws Exception {
    // When / Then
    mvc.perform(request(new Int64StringRequest(9007199254740991L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(9007199254740991L)));
  }

  @Test
  @DisplayName("should return aliased payload when auto alias is requested")
  void shouldReturnAliasedPayloadWhenAutoAliasIsRequested() throws Exception {
    // When / Then
    mvc.perform(request(new AutoAliasRequest("hello")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.a.a", is("hello")));
  }

  @Test
  @DisplayName("should return aliased title when field alias is requested")
  void shouldReturnAliasedTitleWhenFieldAliasIsRequested() throws Exception {
    // When / Then
    mvc.perform(request(new FieldAliasRequest("Webpb")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title", is("Webpb")));
  }

  @Test
  @DisplayName("should return enum string when enum string is requested")
  void shouldReturnEnumStringWhenEnumStringIsRequested() throws Exception {
    // When / Then
    mvc.perform(request(new EnumStringRequest(StatusPb.ACTIVE)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }
}
