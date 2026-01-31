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

package io.github.jinganix.webpb.utilities.utils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ResultWriter")
class ResultWriterTest {

  @Nested
  @DisplayName("write")
  class WriteTest {

    @Nested
    @DisplayName("when failed to write stream")
    class WhenFailedToWriteStreamTest {

      @Test
      @DisplayName("then throw exception")
      void thenThrowExceptionTest() {
        assertThatThrownBy(() -> new ResultWriter(null).write("a", "a"))
            .isInstanceOf(RuntimeException.class);
      }
    }

    @Nested
    @DisplayName("when content is empty")
    class WhenContentIsEmpty {

      @Test
      @DisplayName("then write nothing")
      void thenWriteNothing() {
        assertThatCode(() -> new ResultWriter(null).write("a", null)).doesNotThrowAnyException();
      }
    }
  }
}
