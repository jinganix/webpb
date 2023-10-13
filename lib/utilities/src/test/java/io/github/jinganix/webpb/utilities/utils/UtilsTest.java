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

package io.github.jinganix.webpb.utilities.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void shouldNormalizeStringSuccess() {
    Assertions.assertEquals("", Utils.normalize(""));
    Assertions.assertEquals("", Utils.normalize("/"));
    Assertions.assertEquals("", Utils.normalize("//"));
    Assertions.assertEquals("/a", Utils.normalize("/a"));
    Assertions.assertEquals("/a", Utils.normalize("a/"));
    Assertions.assertEquals("/a", Utils.normalize("/a/"));
    Assertions.assertEquals("/ab.c", Utils.normalize("//ab.c"));
    Assertions.assertEquals("https://ab.c", Utils.normalize("https://ab.c"));
  }

  @Test
  void shouldLimitNewlineSuccess() {
    StringBuilder builder = new StringBuilder();

    Utils.limitNewline(builder, -1);
    assertEquals("", builder.toString());

    Utils.limitNewline(builder, 0);
    assertEquals("", builder.toString());

    builder.append("a");
    Utils.limitNewline(builder, 0);
    assertEquals("a", builder.toString());

    builder.append("\n");
    Utils.limitNewline(builder, 1);
    assertEquals("a\n", builder.toString());

    builder.append("\n\n");
    Utils.limitNewline(builder, 2);
    assertEquals("a\n\n", builder.toString());
  }

  @Test
  void shouldAlignNewlineSuccess() {
    StringBuilder builder = new StringBuilder();

    Utils.alignNewline(builder, -1);
    assertEquals("", builder.toString());

    Utils.alignNewline(builder, 1);
    assertEquals("\n", builder.toString());

    Utils.alignNewline(builder, 0);
    assertEquals("", builder.toString());

    builder.append("a");
    Utils.alignNewline(builder, 0);
    assertEquals("a", builder.toString());

    Utils.alignNewline(builder, 1);
    assertEquals("a\n", builder.toString());

    builder.append("\n");
    Utils.alignNewline(builder, 1);
    assertEquals("a\n", builder.toString());

    builder.append("\n\n");
    Utils.alignNewline(builder, 2);
    assertEquals("a\n\n", builder.toString());
  }

  @Nested
  @DisplayName("toBase52")
  class ToBase52Test {

    @Nested
    @DisplayName("when num is 26")
    class WhenNumIs26Test {

      @Test
      @DisplayName("then base52 string is A")
      void thenBase52StringIsLowercaseA() {
        assertEquals("A", Utils.toBase52(26));
      }
    }

    @Nested
    @DisplayName("when num is 1378")
    class WhenNumIs1378Test {

      @Test
      @DisplayName("then base52 string is AA")
      void thenBase52StringIsLowercaseA() {
        assertEquals("AA", Utils.toBase52(1378));
      }
    }
  }
}
