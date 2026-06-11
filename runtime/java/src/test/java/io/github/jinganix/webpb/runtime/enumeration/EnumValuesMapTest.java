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

package io.github.jinganix.webpb.runtime.enumeration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnumValuesMap")
class EnumValuesMapTest {

  @Test
  @DisplayName("should return value map when class is enumeration")
  void shouldReturnValueMapWhenClassIsEnumeration() {
    // When
    var valueMap = EnumValuesMap.getValueMap(IntegerEnum.class);

    // Then
    assertThat(valueMap).containsKey(1).containsKey("1");
    assertThat(valueMap.get(1)).isEqualTo(IntegerEnum.A);
  }

  @Test
  @DisplayName("should return null when class is not enumeration")
  void shouldReturnNullWhenClassIsNotEnumeration() {
    // When / Then
    assertThat(EnumValuesMap.getValueMap(String.class)).isNull();
  }
}
