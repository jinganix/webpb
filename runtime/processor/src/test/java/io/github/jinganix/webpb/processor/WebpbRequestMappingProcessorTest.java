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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.testing.compile.Compilation;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import org.junit.jupiter.api.Test;

class WebpbRequestMappingProcessorTest {

  @Test
  void shouldProcessSample1Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample1.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample2Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample2.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample3Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample3.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample4Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample4.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample5Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample5.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample6Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample6.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample7Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample7.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSample8Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/Sample8.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldProcessSampleFailed() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbRequestMappingProcessor())
            .compile(forResource("request/SampleFailed1.java"));
    assertThat(compilation).failed();
  }

  @Test
  void shouldInitFailed() {
    WebpbRequestMappingProcessor processor = new WebpbRequestMappingProcessor();
    ProcessingEnvironment env = mock(ProcessingEnvironment.class);
    Messager messager = mock(Messager.class);
    when(env.getMessager()).thenReturn(messager);
    assertThrows(RuntimeException.class, () -> processor.init(env));
  }
}
