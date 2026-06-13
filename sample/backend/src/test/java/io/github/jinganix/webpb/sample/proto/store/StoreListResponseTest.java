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

package io.github.jinganix.webpb.sample.proto.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StoreListResponse")
class StoreListResponseTest {

  @Test
  @DisplayName("should return null paging and stores when unset")
  void shouldReturnNullPagingAndStoresWhenUnset() {
    // Given
    StoreListResponse response = new StoreListResponse().setPaging(null).setStores(null);

    // When / Then
    assertThat(response.webpbMeta()).isNotNull();
    assertThat(response.getPaging()).isNull();
    assertThat(response.getStores()).isNull();
  }
}
