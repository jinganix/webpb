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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EnumValuesMap")
class EnumValuesMapTest {

  @Test
  @DisplayName("should map value and string forms when class is enumeration")
  void shouldMapValueAndStringFormsWhenClassIsEnumeration() {
    // When
    var valueMap = EnumValuesMap.getValueMap(IntegerEnum.class);

    // Then
    assertThat(valueMap)
        .containsEntry(1, IntegerEnum.A)
        .containsEntry("1", IntegerEnum.A)
        .containsEntry("A", IntegerEnum.A);
  }

  @Test
  @DisplayName("should return null when class is not enumeration")
  void shouldReturnNullWhenClassIsNotEnumeration() {
    // When / Then
    assertThat(EnumValuesMap.getValueMap(String.class)).isNull();
  }

  @Nested
  @DisplayName("getValueMap")
  class GetValueMap {

    @Test
    @DisplayName("should return cached value map when called twice")
    void shouldReturnCachedValueMapWhenCalledTwice() {
      // When
      var first = EnumValuesMap.getValueMap(StringEnum.class);
      var second = EnumValuesMap.getValueMap(StringEnum.class);

      // Then
      assertThat(first).isSameAs(second);
      assertThat(first.get("val_a")).isEqualTo(StringEnum.A);
    }
  }
}
