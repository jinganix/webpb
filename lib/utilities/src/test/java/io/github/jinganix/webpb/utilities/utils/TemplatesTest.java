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

package io.github.jinganix.webpb.utilities.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Templates")
class TemplatesTest {

  @Nested
  @DisplayName("process")
  class ProcessTest {

    @Nested
    @DisplayName("when template not found")
    class WhenTemplateNotFoundTest {

      @Test
      @DisplayName("then throw exception")
      void thenThrowExceptionTest() {
        assertThrows(
            RuntimeException.class,
            () -> new Templates().process("non_exists.ftl", new HashMap<>()));
      }
    }
  }
}
