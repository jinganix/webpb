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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class GeneratorTest {

  @Test
  void test() {
    for (Dump dump : Dump.values()) {
      RequestContext context = createRequest(dump);
      Generator generator = Generator.create();
      for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
        String content = generator.generate(fileDescriptor);
        if (StringUtils.isEmpty(content)) {
          continue;
        }
        String prefix = "/" + dump.name().toLowerCase() + "/";
        String filename = prefix + fileDescriptor.getPackage() + ".ts";
        String expected = TestUtils.readFile(filename);

        assertEquals(expected, content);
      }
    }
  }
}
