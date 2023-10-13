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

package io.github.jinganix.webpb.utilities.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jinganix.webpb.tests.Dump;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("TestUtils")
class TestUtilsTest {

  @Test
  void shouldCreateRequestContextSuccess() {
    Assertions.assertDoesNotThrow(() -> TestUtils.createRequest(Dump.test1));
  }

  @Test
  void shouldCreateRequestThrowExceptionWhenInputError() {
    System.setIn(new ByteArrayInputStream("abc".getBytes()));
    Dump dump = Mockito.mock(Dump.class);
    assertThrows(RuntimeException.class, () -> TestUtils.createRequest(dump));
  }

  @Test
  void shouldCompareToFileSuccess() {
    Assertions.assertTrue(TestUtils.compareToFile("abcd\"ef\"g'hi'", "/compare.txt", false));
    Assertions.assertFalse(TestUtils.compareToFile("abcd\"ef\"g'hi'", "/compare.txt", true));
    assertThrows(RuntimeException.class, () -> TestUtils.compareToFile("", "/any", true));
  }

  @Nested
  @DisplayName("readFile")
  class ReadFileClassTest {

    @Nested
    @DisplayName("when file not found")
    class WhenFileNotFoundTest {

      @Test
      @DisplayName("then throw exception")
      void thenThrowExceptionTest() {
        assertThrows(RuntimeException.class, () -> TestUtils.readFile("non_exists"));
      }
    }
  }
}
