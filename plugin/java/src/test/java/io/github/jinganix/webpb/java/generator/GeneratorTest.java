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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GeneratorTest {

  void testDump(Dump dump) {
    RequestContext context = TestUtils.createRequest(dump);
    for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
      Map<String, String> fileMap = Generator.create().generate(fileDescriptor);
      for (Map.Entry<String, String> entry : fileMap.entrySet()) {
        String key = entry.getKey().replaceFirst("^/", "");
        String expected = TestUtils.readFile("/" + dump + "/" + key);
        assertEquals(expected, entry.getValue());
      }
    }
  }

  @Test
  void auto_alias() {
    testDump(Dump.auto_alias);
  }

  @Test
  void extends_test() {
    testDump(Dump.extends_test);
  }

  @Test
  void error_test() {
    RequestContext context = TestUtils.createRequest(Dump.error_test);
    for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
      assertThrows(RuntimeException.class, () -> Generator.create().generate(fileDescriptor));
    }
  }

  @Test
  void import_test() {
    testDump(Dump.import_test);
  }

  @Test
  void test1() {
    testDump(Dump.test1);
  }

  @Test
  void test2() {
    testDump(Dump.test2);
  }

  @Test
  void test3() {
    RequestContext context = TestUtils.createRequest(Dump.test3);
    for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
      assertThrows(RuntimeException.class, () -> Generator.create().generate(fileDescriptor));
    }
  }
}
