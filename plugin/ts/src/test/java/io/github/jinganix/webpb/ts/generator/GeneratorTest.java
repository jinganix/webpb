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

package io.github.jinganix.webpb.ts.generator;

import static io.github.jinganix.webpb.utilities.test.TestUtils.createRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@DisplayName("Generator")
class GeneratorTest {

  static class TestArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      List<Arguments> argumentsList = new ArrayList<>();
      for (Dump dump : Dump.values()) {
        RequestContext request = createRequest(dump);
        for (FileDescriptor fileDescriptor : request.getTargetDescriptors()) {
          String prefix = "/" + dump.name().toLowerCase() + "/";
          String filename = prefix + fileDescriptor.getPackage() + ".ts";
          argumentsList.add(Arguments.of(dump.name(), filename, fileDescriptor));
        }
      }
      return argumentsList.stream();
    }
  }

  @Nested
  @DisplayName("generate")
  class AutoAlias {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      Generator generator = new Generator();

      @ParameterizedTest(name = "{0} => {1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(TestArgumentsProvider.class)
      void thenGenerateExpected(String dump, String filename, FileDescriptor fileDescriptor) {
        assertThat(dump).isNotEmpty();
        String expected;
        try {
          expected = TestUtils.readFile(filename);
        } catch (NullPointerException e) {
          expected = null;
        }
        assertThat(generator.generate(fileDescriptor)).isEqualTo(expected);
      }
    }
  }
}
