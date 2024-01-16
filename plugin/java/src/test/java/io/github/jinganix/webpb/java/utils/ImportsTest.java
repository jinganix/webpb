package io.github.jinganix.webpb.java.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Imports")
public class ImportsTest {

  @Nested
  @DisplayName("importedQualifiedName")
  class ImportedQualifiedName {

    @Nested
    @DisplayName("when name not imported")
    class WhenNameNotImported {

      @Test
      @DisplayName("then return itself")
      void thenReturnItself() {
        Imports imports = new Imports("abc", new ArrayList<>());
        assertThat(imports.importedQualifiedName("a.b")).isEqualTo("a.b");
      }
    }
  }
}
