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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbUtils;
import io.github.jinganix.webpb.runtime.reactive.WebpbClient;
import io.github.jinganix.webpb.sample.proto.common.PageablePb;
import io.github.jinganix.webpb.sample.proto.store.StoreGreetingRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreGreetingResponse;
import io.github.jinganix.webpb.sample.proto.store.StoreListRequest;
import io.github.jinganix.webpb.sample.proto.store.StoreVisitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@DisplayName("StoreController")
class StoreControllerTest {

  @Autowired private MockMvc mvc;

  @MockitoBean private WebpbClient webpbClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockHttpServletRequestBuilder request(WebpbMessage message) {
    return MockMvcRequestBuilders.request(
            HttpMethod.valueOf(message.webpbMeta().getMethod()), WebpbUtils.formatUrl(message))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(message));
  }

  @Nested
  @DisplayName("getStore")
  class GetStore {

    @Nested
    @DisplayName("when request is performed")
    class WhenRequestIsPerformed {

      @Test
      @DisplayName("then response store")
      void thenResponseStore() throws Exception {
        int storeId = 123;
        String customer = "fakeName";
        when(webpbClient.request(any(), any()))
            .thenReturn(new StoreGreetingResponse("Welcome, " + customer));

        mvc.perform(
                request(new StoreVisitRequest((long) storeId, customer))
                    .content("{\"customer\": \"" + customer + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.a.a", is(storeId)))
            .andExpect(jsonPath("$.a.b", is("store-" + storeId)))
            .andExpect(jsonPath("$.a.c", is("Chengdu")))
            .andExpect(jsonPath("$.b", is("Welcome, " + customer)));
      }
    }
  }

  @Nested
  @DisplayName("GetStores")
  class GetStores {

    @Nested
    @DisplayName("when page size is 11")
    class WhenPageSizeIs11 {

      @Test
      @DisplayName("then response error")
      void thenResponseError() throws Exception {
        mvc.perform(request(new StoreListRequest(new PageablePb(true, 2, 11, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.['pageable.size']", is("must be between 1 and 10")));
        ;
      }
    }

    @Nested
    @DisplayName("when pageable is null")
    class WhenPageableIsNull {

      @Test
      @DisplayName("then response stores")
      void thenResponseStores() throws Exception {
        mvc.perform(request(new StoreListRequest(null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paging.page", is(1)))
            .andExpect(jsonPath("$.stores", hasSize(10)));
      }
    }

    @Nested
    @DisplayName("when size and page of pageable is null")
    class WhenSizeAndPageOfPageableIsNull {

      @Test
      @DisplayName("then response stores")
      void thenResponseStores() throws Exception {
        mvc.perform(request(new StoreListRequest(new PageablePb())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paging.page", is(1)))
            .andExpect(jsonPath("$.stores", hasSize(10)));
      }
    }

    @Nested
    @DisplayName("when size and page of pageable is not null")
    class WhenSizeAndPageOfPageableIsNotNull {

      @Test
      @DisplayName("then response stores")
      void thenResponseStores() throws Exception {
        mvc.perform(request(new StoreListRequest(new PageablePb(true, 2, 8, ""))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paging.page", is(2)))
            .andExpect(jsonPath("$.stores", hasSize(8)));
      }
    }
  }

  @Nested
  @DisplayName("greeting")
  class Greeting {

    @Nested
    @DisplayName("when request is performed")
    class WhenRequestIsPerformed {

      @Test
      @DisplayName("then response greeting message")
      void thenResponseGreetingMessage() throws Exception {
        String customer = "fakeName";
        mvc.perform(
                request(new StoreGreetingRequest(customer))
                    .content("{\"customer\": \"" + customer + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.greeting", is("Welcome, " + customer)));
      }
    }
  }
}
