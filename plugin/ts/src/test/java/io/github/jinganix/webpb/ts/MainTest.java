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

package io.github.jinganix.webpb.ts;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.jinganix.webpb.tests.Dump;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Main")
class MainTest {

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Nested
    @DisplayName("when called")
    class WhenCalled {

      @Test
      @DisplayName("then concrete")
      void thenConcrete() {
        assertThatCode(Main::new).doesNotThrowAnyException();
      }
    }
  }

  @Nested
  @DisplayName("main")
  class MainMethod {

    @Nested
    @DisplayName("when called")
    class WhenCalled {

      @Test
      @DisplayName("then no errors")
      void thenNoErrors() {
        Dump.extends_test.pipe();
        assertThatCode(() -> Main.main(null)).doesNotThrowAnyException();
      }
    }
  }
}
