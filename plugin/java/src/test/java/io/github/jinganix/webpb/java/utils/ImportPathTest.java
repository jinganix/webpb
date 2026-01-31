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

package io.github.jinganix.webpb.java.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@DisplayName("ImportPath")
class ImportPathTest {

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Nested
    @DisplayName("when path is a.b.c")
    class WhenPathIsABC {

      @Test
      @DisplayName("then concrete")
      void thenConcrete() {
        ImportPath importPath = new ImportPath("a.b.c");
        assertThat(importPath.getPath()).isEqualTo("a.b.c");
        assertThat(importPath.getIdentifier()).isEqualTo("c");
      }
    }

    @Nested
    @DisplayName("when path is a..b")
    class WhenPathIsAB {

      @Test
      @DisplayName("then throw")
      void thenThrow() {
        assertThatThrownBy(() -> new ImportPath("a..b"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Invalid import path: a..b");
      }
    }
  }

  @Nested
  @DisplayName("equals")
  class Equals {

    ImportPath importPath = new ImportPath("a.b.c");

    @Nested
    @DisplayName("when path is a.b.c")
    class WhenPathIsABC {

      @Test
      @DisplayName("then equal")
      void thenEqual() {
        assertThat(importPath.equals(new ImportPath("a.b.c"))).isTrue();
      }
    }

    @Nested
    @DisplayName("when path is a.b")
    class WhenPathIsAB {

      @Test
      @DisplayName("then not equal")
      void thenNotEqual() {
        assertThat(importPath.equals(new ImportPath("a.b"))).isFalse();
      }
    }

    @Nested
    @DisplayName("when path is different class")
    class WhenPathIsDifferentClass {

      @Test
      @DisplayName("then not equal")
      void thenNotEqual() {
        assertThat(importPath.equals(new ImportPathTest())).isFalse();
      }
    }
  }

  static class TestArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("a.b.c", "c", "c"),
          Arguments.of("a.b.c", "c.d", "c.d"),
          Arguments.of("a.b.c", "b.c.d", "c.d"),
          Arguments.of("a.b", "b.c", "b.c"),
          Arguments.of("a.b.c", "e.c.d", null),
          Arguments.of("a.b.c", "e.d", null),
          Arguments.of("a.b.c", "c\\", null),
          Arguments.of("a.b.c", "c..d", null));
    }
  }

  @Nested
  @DisplayName("resolve")
  class Resolve {

    @Nested
    @DisplayName("when paths provided")
    class WhenDumpPathsProvided {

      @ParameterizedTest(name = "{0}.relative({1} => {2}")
      @DisplayName("then return relative path")
      @ArgumentsSource(TestArgumentsProvider.class)
      void thenReturnRelativePath(String path, String name, String expected) {
        assertThat(new ImportPath(path).relative(name)).isEqualTo(expected);
      }
    }
  }
}
