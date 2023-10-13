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

package io.github.jinganix.webpb.java.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  static List<Arguments> getArgumentsList(Dump dump) {
    RequestContext context = TestUtils.createRequest(dump);
    List<Arguments> argumentsList = new ArrayList<>();
    for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
      Map<String, String> fileMap = Generator.create().generate(fileDescriptor);
      for (Map.Entry<String, String> entry : fileMap.entrySet()) {
        String key = entry.getKey().replaceFirst("^/", "");
        String expected = TestUtils.readFile("/" + dump + "/" + key);
        assertThat(entry.getValue()).isEqualTo(expected);
        argumentsList.add(Arguments.of(dump.name(), key, entry.getValue(), expected));
      }
    }
    return argumentsList;
  }

  static class AutoAliasArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return getArgumentsList(Dump.auto_alias).stream();
    }
  }

  @Nested
  @DisplayName("auto_alias")
  class AutoAlias {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(AutoAliasArgumentsProvider.class)
      void thenGenerateExpected(String dump, String key, String content, String expected) {
        assertThat(dump).isEqualTo("auto_alias");
        assertThat(key).isNotEmpty();
        assertThat(content).isEqualTo(expected);
      }
    }
  }

  static class ExtendsTestArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return getArgumentsList(Dump.extends_test).stream();
    }
  }

  @Nested
  @DisplayName("extends_test")
  class ExtendsTest {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(ExtendsTestArgumentsProvider.class)
      void thenGenerateExpected(String dump, String key, String content, String expected) {
        assertThat(dump).isEqualTo("extends_test");
        assertThat(key).isNotEmpty();
        assertThat(content).isEqualTo(expected);
      }
    }
  }

  static class ImportTestArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return getArgumentsList(Dump.import_test).stream();
    }
  }

  @Nested
  @DisplayName("import_test")
  class ImportTest {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(ImportTestArgumentsProvider.class)
      void thenGenerateExpected(String dump, String key, String content, String expected) {
        assertThat(dump).isEqualTo("import_test");
        assertThat(key).isNotEmpty();
        assertThat(content).isEqualTo(expected);
      }
    }
  }

  static class Test1ArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return getArgumentsList(Dump.test1).stream();
    }
  }

  @Nested
  @DisplayName("test1")
  class Test1 {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(Test1ArgumentsProvider.class)
      void thenGenerateExpected(String dump, String key, String content, String expected) {
        assertThat(dump).isEqualTo("test1");
        assertThat(key).isNotEmpty();
        assertThat(content).isEqualTo(expected);
      }
    }
  }

  static class Test2ArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return getArgumentsList(Dump.test2).stream();
    }
  }

  @Nested
  @DisplayName("test2")
  class Test2 {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(Test2ArgumentsProvider.class)
      void thenGenerateExpected(String dump, String key, String content, String expected) {
        assertThat(dump).isEqualTo("test2");
        assertThat(key).isNotEmpty();
        assertThat(content).isEqualTo(expected);
      }
    }
  }

  static class ErrorTestArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return TestUtils.createRequest(Dump.error_test).getTargetDescriptors().stream()
          .map(Arguments::of);
    }
  }

  @Nested
  @DisplayName("error_test")
  class ErrorTest {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(ErrorTestArgumentsProvider.class)
      void thenGenerateExpected(FileDescriptor descriptor) {
        assertThatThrownBy(() -> Generator.create().generate(descriptor))
            .isInstanceOf(RuntimeException.class);
      }
    }
  }

  static class Test3ArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return TestUtils.createRequest(Dump.test3).getTargetDescriptors().stream().map(Arguments::of);
    }
  }

  @Nested
  @DisplayName("test3")
  class Test3 {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      @ParameterizedTest(name = "{1}")
      @DisplayName("then generate expected")
      @ArgumentsSource(Test3ArgumentsProvider.class)
      void thenGenerateExpected(FileDescriptor descriptor) {
        assertThatThrownBy(() -> Generator.create().generate(descriptor))
            .isInstanceOf(RuntimeException.class);
      }
    }
  }
}
