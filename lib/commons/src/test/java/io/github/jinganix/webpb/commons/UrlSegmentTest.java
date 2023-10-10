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

package io.github.jinganix.webpb.commons;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UrlSegment")
class UrlSegmentTest {

  @Nested
  @DisplayName("constructor")
  class ConstructorTest {

    @Nested
    @DisplayName("when value is {")
    class WhenValueIsLeftParenthesisTest {

      @Test
      @DisplayName("then is not accessor")
      void thenAccessorIsTrueTest() {
        UrlSegment urlSegment = new UrlSegment("", "", "{");
        assertFalse(urlSegment.isAccessor());
      }
    }

    @Nested
    @DisplayName("when value is }")
    class WhenValueIsRightParenthesisTest {

      @Test
      @DisplayName("then is not accessor")
      void thenAccessorIsTrueTest() {
        UrlSegment urlSegment = new UrlSegment("", "", "}");
        assertFalse(urlSegment.isAccessor());
      }
    }

    @Nested
    @DisplayName("when key is null")
    class WhenKeyIsNullTest {

      @Test
      @DisplayName("then is not query")
      void thenQueryIsTrueTest() {
        UrlSegment urlSegment = new UrlSegment("", null, "{}");
        assertFalse(urlSegment.isQuery());
      }
    }

    @Nested
    @DisplayName("when key is empty")
    class WhenKeyIsEmptyTest {

      @Test
      @DisplayName("then is not query")
      void thenQueryIsTrueTest() {
        UrlSegment urlSegment = new UrlSegment("", "", "{}");
        assertFalse(urlSegment.isQuery());
      }
    }
  }
}
