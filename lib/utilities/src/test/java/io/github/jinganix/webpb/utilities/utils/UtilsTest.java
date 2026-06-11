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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Utils")
class UtilsTest {

  @Test
  @DisplayName("should normalize path strings when given varied inputs")
  void shouldNormalizePathStringsWhenGivenVariedInputs() {
    // When / Then
    assertThat(Utils.normalize("")).isEmpty();
    assertThat(Utils.normalize("/")).isEmpty();
    assertThat(Utils.normalize("//")).isEmpty();
    assertThat(Utils.normalize("/a")).isEqualTo("/a");
    assertThat(Utils.normalize("a/")).isEqualTo("/a");
    assertThat(Utils.normalize("/a/")).isEqualTo("/a");
    assertThat(Utils.normalize("//ab.c")).isEqualTo("/ab.c");
    assertThat(Utils.normalize("https://ab.c")).isEqualTo("https://ab.c");
  }

  @Test
  @DisplayName("should limit trailing newlines when count is specified")
  void shouldLimitTrailingNewlinesWhenCountIsSpecified() {
    // Given
    StringBuilder builder = new StringBuilder();

    // When / Then
    Utils.limitNewline(builder, -1);
    assertThat(builder).isEmpty();

    Utils.limitNewline(builder, 0);
    assertThat(builder).isEmpty();

    builder.append("a");
    Utils.limitNewline(builder, 0);
    assertThat(builder).hasToString("a");

    builder.append("\n");
    Utils.limitNewline(builder, 1);
    assertThat(builder).hasToString("a\n");

    builder.append("\n\n");
    Utils.limitNewline(builder, 2);
    assertThat(builder).hasToString("a\n\n");
  }

  @Test
  @DisplayName("should align trailing newlines when count is specified")
  void shouldAlignTrailingNewlinesWhenCountIsSpecified() {
    // Given
    StringBuilder builder = new StringBuilder();

    // When / Then
    Utils.alignNewline(builder, -1);
    assertThat(builder).isEmpty();

    Utils.alignNewline(builder, 1);
    assertThat(builder).hasToString("\n");

    Utils.alignNewline(builder, 0);
    assertThat(builder).isEmpty();

    builder.append("a");
    Utils.alignNewline(builder, 0);
    assertThat(builder).hasToString("a");

    Utils.alignNewline(builder, 1);
    assertThat(builder).hasToString("a\n");

    builder.append("\n");
    Utils.alignNewline(builder, 1);
    assertThat(builder).hasToString("a\n");

    builder.append("\n\n");
    Utils.alignNewline(builder, 2);
    assertThat(builder).hasToString("a\n\n");
  }

  @Test
  @DisplayName("should return A when num is 26")
  void shouldReturnAWhenNumIs26() {
    // When / Then
    assertThat(Utils.toBase52(26)).isEqualTo("A");
  }

  @Test
  @DisplayName("should return AA when num is 1378")
  void shouldReturnAAWhenNumIs1378() {
    // When / Then
    assertThat(Utils.toBase52(1378)).isEqualTo("AA");
  }
}
