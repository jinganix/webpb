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

package io.github.jinganix.webpb.java.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ImportPath")
class ImportPathTest {

  @Test
  @DisplayName("equals")
  void equalsTest() {
    ImportPath importPath = new ImportPath("a.b.c");
    assertEquals(new ImportPath("a.b.c"), importPath);
    assertNotEquals(new ImportPath("a.b"), importPath);
    assertFalse(importPath.equals(new ImportPathTest()));
  }

  @Test
  void shouldTestConstructSuccessTest() {
    ImportPath importPath = new ImportPath("a.b.c");
    assertEquals("a.b.c", importPath.getPath());
    assertEquals("c", importPath.getIdentifier());

    assertThrows(RuntimeException.class, () -> new ImportPath("a..b"), "Invalid name: a..b");
  }

  @Test
  void shouldResolveSuccessTest() {
    assertEquals("c", new ImportPath("a.b.c").relative("c"));
    assertEquals("c.d", new ImportPath("a.b.c").relative("c.d"));
    assertEquals("c.d", new ImportPath("a.b.c").relative("b.c.d"));
    assertEquals("b.c", new ImportPath("a.b").relative("b.c"));

    assertNull(new ImportPath("a.b.c").relative("e.c.d"));
    assertNull(new ImportPath("a.b.c").relative("e.d"));
    assertNull(new ImportPath("a.b.c").relative("c\\"));
    assertNull(new ImportPath("a.b.c").relative("c..d"));
  }
}
