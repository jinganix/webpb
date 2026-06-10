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

package io.github.jinganix.webpb.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.testing.compile.Compilation;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WebpbRequestMappingProcessor")
class WebpbRequestMappingProcessorTest {

  @Test
  @DisplayName("should compile successfully when processing sample1")
  void shouldCompileSuccessfullyWhenProcessingSample1() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample1.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample2")
  void shouldCompileSuccessfullyWhenProcessingSample2() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample2.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample3")
  void shouldCompileSuccessfullyWhenProcessingSample3() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample3.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample4")
  void shouldCompileSuccessfullyWhenProcessingSample4() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample4.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample5")
  void shouldCompileSuccessfullyWhenProcessingSample5() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample5.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample6")
  void shouldCompileSuccessfullyWhenProcessingSample6() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample6.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample7")
  void shouldCompileSuccessfullyWhenProcessingSample7() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample7.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should compile successfully when processing sample8")
  void shouldCompileSuccessfullyWhenProcessingSample8() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample8.java"));

    // Then
    assertThat(compilation).succeeded();
  }

  @Test
  @DisplayName("should fail compilation when processing invalid sample")
  void shouldFailCompilationWhenProcessingInvalidSample() {
    // When
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/SampleFailed1.java"));

    // Then
    assertThat(compilation).failed();
  }

  @Test
  @DisplayName("should throw when init is called without required services")
  void shouldThrowWhenInitIsCalledWithoutRequiredServices() {
    // Given
    WebpbRequestMappingProcessor processor = new WebpbRequestMappingProcessor();
    ProcessingEnvironment env = mock(ProcessingEnvironment.class);
    Messager messager = mock(Messager.class);
    when(env.getMessager()).thenReturn(messager);

    // When / Then
    assertThatThrownBy(() -> processor.init(env)).isInstanceOf(RuntimeException.class);
  }
}
