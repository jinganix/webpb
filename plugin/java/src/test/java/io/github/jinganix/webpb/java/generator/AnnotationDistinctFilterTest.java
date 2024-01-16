package io.github.jinganix.webpb.java.generator;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.jinganix.webpb.java.utils.Imports;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AnnotationDistinctFilter")
class AnnotationDistinctFilterTest {

  @Nested
  @DisplayName("test")
  class TestTest {

    @Nested
    @DisplayName("when parse failed")
    class WhenParseFailed {

      @Test
      @DisplayName("then throws exception")
      void thenThrowsException() {
        Imports imports = mock(Imports.class);
        AnnotationDistinctFilter filter = new AnnotationDistinctFilter(imports, emptyList());
        assertThatThrownBy(() -> filter.test("abc"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Bad annotation: abc");
      }
    }

    @Nested
    @DisplayName("when is repeatable")
    class WhenIsRepeatable {

      @Test
      @DisplayName("then return true")
      void thenReturnTrue() {
        Imports imports = mock(Imports.class);
        when(imports.importedQualifiedName("b")).thenReturn("a.b");
        AnnotationDistinctFilter filter = new AnnotationDistinctFilter(imports, Lists.list("a.b"));
        assertThat(filter.test("@b")).isTrue();
      }
    }

    @Nested
    @DisplayName("when is not repeatable")
    class WhenIsNotRepeatable {

      @Nested
      @DisplayName("when not exists")
      class WhenNotExists {

        @Test
        @DisplayName("then return true")
        void thenReturnTrue() {
          Imports imports = mock(Imports.class);
          when(imports.importedQualifiedName("b")).thenReturn("a.b");
          AnnotationDistinctFilter filter = new AnnotationDistinctFilter(imports, emptyList());
          assertThat(filter.test("@b")).isTrue();
        }
      }

      @Nested
      @DisplayName("when exists")
      class WhenExists {

        @Test
        @DisplayName("then return false")
        void thenReturnFalse() {
          Imports imports = mock(Imports.class);
          when(imports.importedQualifiedName("b")).thenReturn("a.b");
          AnnotationDistinctFilter filter = new AnnotationDistinctFilter(imports, emptyList());
          assertThat(filter.test("@b")).isTrue();
          assertThat(filter.test("@b")).isFalse();
        }
      }
    }
  }
}
