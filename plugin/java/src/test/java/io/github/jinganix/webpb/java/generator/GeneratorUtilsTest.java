/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
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
 *
 * https://github.com/jinganix/webpb
 */

package io.github.jinganix.webpb.java.generator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jinganix.webpb.java.utils.GeneratorUtils;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AnnotationUtils")
class GeneratorUtilsTest {

  @Nested
  @DisplayName("exists")
  class ExistsTest {

    @Nested
    @DisplayName("when annotation is invalid")
    class WhenAnnotationIsInvalidTest {

      @Test
      @DisplayName("then throw exception")
      void thenThrowExceptionTest() {
        assertThrows(
            RuntimeException.class, () -> GeneratorUtils.exists(Collections.emptyList(), "a..."));
      }
    }

    @Nested
    @DisplayName("when annotations contains invalid annotation")
    class WhenAnnotationsContainsInvalidTest {

      @Test
      @DisplayName("then throw exception")
      void thenThrowExceptionTest() {
        assertThrows(
            RuntimeException.class,
            () -> GeneratorUtils.exists(Collections.singletonList("a..."), "@Anno"));
      }
    }

    @Nested
    @DisplayName("when annotations contains the annotation")
    class WhenAnnotationsContainsTheAnnotationTest {

      @Test
      @DisplayName("then return true")
      void thenReturnTrue() {
        assertTrue(GeneratorUtils.exists(Arrays.asList("@Anno1", "@Anno2"), "@Anno2"));
      }
    }
  }
}
