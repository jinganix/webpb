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

package io.github.jinganix.webpb.processor;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import io.github.jinganix.webpb.processor.misc.JvmOpens;
import io.github.jinganix.webpb.processor.misc.TreeMakerImport;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TreeMakerImport")
class TreeMakerImportTest {

  @BeforeEach
  void setup() {
    JvmOpens.addOpens(TreeMakerImportTest.class);
  }

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Nested
    @DisplayName("when method found")
    class WhenMethodFound {

      @Test
      @DisplayName("then concrete")
      void thenConcrete() {
        assertThatCode(TreeMakerImport::new).doesNotThrowAnyException();
      }
    }
  }

  @Nested
  @DisplayName("Import")
  class Import {

    @Nested
    @DisplayName("when called")
    class WhenCalled {

      @Test
      @DisplayName("then return")
      void thenReturn() {
        TreeMakerImport treeMakerImport = new TreeMakerImport();
        assertThatCode(() -> treeMakerImport.Import(mock(TreeMaker.class), null, true))
            .doesNotThrowAnyException();
      }
    }

    @Nested
    @DisplayName("when has error")
    class WhenHasError {

      @Test
      @DisplayName("then throw exception")
      void thenThrowException() throws Exception {
        TreeMakerImport treeMakerImport = new TreeMakerImport();
        Field field = treeMakerImport.getClass().getDeclaredField("importMethod");
        field.setAccessible(true);
        Method method = mock(Method.class);
        field.set(treeMakerImport, method);
        when(method.invoke(any(), any(), any())).thenThrow(new RuntimeException());
        assertThatThrownBy(
                () -> treeMakerImport.Import(mock(TreeMaker.class), mock(JCTree.class), true))
            .isInstanceOf(RuntimeException.class);
      }
    }
  }
}
