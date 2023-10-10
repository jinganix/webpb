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

class MixedProcessorTest {

  @Test
  void shouldProcessSample1Success() {
    Compilation compilation =
        javac()
            .withProcessors(new WebpbMessageMappingProcessor(), new WebpbRequestMappingProcessor())
            .compile(forResource("mixed/Sample1.java"));
    assertThat(compilation).succeeded();
  }

  @Test
  void shouldInitFailed() {
    WebpbMessageMappingProcessor processor = new WebpbMessageMappingProcessor();
    ProcessingEnvironment env = mock(ProcessingEnvironment.class);
    Messager messager = mock(Messager.class);
    when(env.getMessager()).thenReturn(messager);
    assertThrows(RuntimeException.class, () -> processor.init(env));
  }
}
