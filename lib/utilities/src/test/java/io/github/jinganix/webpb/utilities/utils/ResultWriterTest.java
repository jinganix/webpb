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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResultWriter")
class ResultWriterTest {

  @Test
  @DisplayName("should throw when write stream fails")
  void shouldThrowWhenWriteStreamFails() {
    // When / Then
    assertThatThrownBy(() -> new ResultWriter(null).write("a", "a"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should not write when content is empty")
  void shouldNotWriteWhenContentIsEmpty() {
    // When / Then
    assertThatCode(() -> new ResultWriter(null).write("a", null)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("should write protobuf response when content is not empty")
  void shouldWriteProtobufResponseWhenContentIsNotEmpty() {
    // Given
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(buffer);

    // When
    new ResultWriter(stream).write("a.java", "class A {}");

    // Then
    assertThatCode(buffer::toByteArray).doesNotThrowAnyException();
  }
}
