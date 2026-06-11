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

package io.github.jinganix.webpb.utilities.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import io.github.jinganix.webpb.tests.Dump;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("TestUtils")
class TestUtilsTest {

  @Test
  @DisplayName("should create request when dump is valid")
  void shouldCreateRequestWhenDumpIsValid() {
    // When / Then
    assertThat(TestUtils.createRequest(Dump.proto2_core_codegen)).isNotNull();
  }

  @Test
  @DisplayName("should throw when dump is invalid")
  void shouldThrowWhenDumpIsInvalid() {
    // Given
    System.setIn(new ByteArrayInputStream("abc".getBytes()));
    Dump dump = mock(Dump.class);

    // When / Then
    assertThatThrownBy(() -> TestUtils.createRequest(dump)).isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("should throw when file is not found")
  void shouldThrowWhenFileIsNotFound() {
    // When / Then
    assertThatThrownBy(() -> TestUtils.readFile("non_exists"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("File not found: non_exists");
  }

  @Test
  @DisplayName("should throw when read stream fails")
  void shouldThrowWhenReadStreamFails() {
    // Given
    try (MockedStatic<IOUtils> ioUtils = Mockito.mockStatic(IOUtils.class)) {
      ioUtils
          .when(() -> IOUtils.toString(any(InputStream.class), eq(StandardCharsets.UTF_8)))
          .thenThrow(new IOException());

      // When / Then
      assertThatThrownBy(() -> TestUtils.readFile("/foo.test"))
          .isInstanceOf(RuntimeException.class)
          .hasCauseInstanceOf(IOException.class);
    }
  }

  @Test
  @DisplayName("should write file when write succeeds")
  void shouldWriteFileWhenWriteSucceeds() {
    // Given
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
      fileUtils
          .when(
              () ->
                  FileUtils.writeStringToFile(
                      any(File.class), anyString(), eq(StandardCharsets.UTF_8)))
          .thenAnswer(x -> null);

      // When / Then
      assertThatCode(() -> TestUtils.writeFile(mock(File.class), "/hello"))
          .doesNotThrowAnyException();
    }
  }

  @Test
  @DisplayName("should throw when write file fails")
  void shouldThrowWhenWriteFileFails() {
    // Given
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
      fileUtils
          .when(
              () ->
                  FileUtils.writeStringToFile(
                      any(File.class), anyString(), eq(StandardCharsets.UTF_8)))
          .thenThrow(new IOException());

      // When / Then
      assertThatThrownBy(() -> TestUtils.writeFile(mock(File.class), "/hello"))
          .isInstanceOf(RuntimeException.class)
          .hasCauseInstanceOf(IOException.class);
    }
  }
}
