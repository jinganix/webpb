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

package io.github.jinganix.webpb.ts.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Imports")
class ImportsTest {

  @Nested
  @DisplayName("importPath")
  class ImportPathTest {

    @Nested
    @DisplayName("when name equals this package")
    class WhenNameEqualsThisPackageTest {

      @Test
      @DisplayName("then do not import")
      void thenDoNotImportTest() {
        Imports imports = new Imports("a", new ArrayList<>(), new ArrayList<>());
        imports.importPath(new ImportPath("a", "./a"));
        assertTrue(imports.toList().isEmpty());
      }
    }
  }

  @Nested
  @DisplayName("importType")
  class ImportTypeTest {

    @Nested
    @DisplayName("when type starts with this package")
    class WhenTypeOrPackageStartsWithThisPackageTest {

      @Test
      @DisplayName("then do not import")
      void thenDoNotImportTest() {
        Imports imports = new Imports("a", new ArrayList<>(), new ArrayList<>());
        imports.importType("a/b");
        assertTrue(imports.toList().isEmpty());
      }
    }

    @Nested
    @DisplayName("when type is found")
    class WhenTypeIsFoundTest {

      @Test
      @DisplayName("then import the package")
      void thenImportThePackageTest() {
        Imports imports = new Imports("a", new ArrayList<>(), Arrays.asList("c/d", "c/e", "c/f"));
        assertEquals("c.e", imports.importType("e"));
        assertEquals(1, imports.toList().size());
        assertEquals("import * as c from \"c\";", imports.toList().get(0));
      }
    }

    @Nested
    @DisplayName("when type is not found")
    class WhenTypeIsNotFoundTest {

      @Test
      @DisplayName("then not export")
      void thenThrowException() {
        Imports imports = new Imports("a", new ArrayList<>(), new ArrayList<>());
        assertEquals("f", imports.importType("f"));
        assertTrue(imports.toList().isEmpty());
      }
    }
  }

  @Nested
  @DisplayName("toList")
  class ToListTest {

    @Nested
    @DisplayName("when import path has order")
    class WhenImportPathHasOrderTest {

      @Test
      @DisplayName("then import list is sorted")
      void thenImportListIsSortedTest() {
        Imports imports = new Imports("p", new ArrayList<>(), new ArrayList<>());
        imports.importPath(new ImportPath("a", "./a", 2));
        imports.importPath(new ImportPath("b", "./b", 1));
        assertEquals(2, imports.toList().size());
        assertEquals("import * as b from \"./b\";", imports.toList().get(0));
        assertEquals("import * as a from \"./a\";", imports.toList().get(1));
      }
    }
  }
}
