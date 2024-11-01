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

import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExtendsGenerator")
class ExtendsGeneratorTest {

  @Nested
  @DisplayName("generate")
  class Generate {

    @Nested
    @DisplayName("when dump files provided")
    class WhenDumpFilesProvided {

      ExtendsGenerator generator = new ExtendsGenerator();

      @Test
      @DisplayName("then generate expected")
      void thenGenerateExpected() {
        for (Dump dump : Dump.values()) {
          RequestContext request = createRequest(dump);
          Map<String, String> data = generator.generate(request);
          for (Entry<String, String> entry : data.entrySet()) {
            String filename = "/" + dump.name().toLowerCase() + "/" + entry.getKey();
            String expected;
            try {
              expected = TestUtils.readFile(filename);
            } catch (NullPointerException e) {
              expected = null;
            }
            if ("true".equals(System.getenv().get("DUMP_TEST_FILES"))) {
              String cwd = System.getProperty("user.dir");
              File file = Paths.get(cwd, "src/test/resources", filename).toFile();
              TestUtils.writeFile(file, entry.getValue());
            }
            assertThat(entry.getValue()).isEqualTo(expected);
          }
        }
      }
    }
  }
}
