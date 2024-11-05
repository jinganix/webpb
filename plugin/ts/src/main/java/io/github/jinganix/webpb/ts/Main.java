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

package io.github.jinganix.webpb.ts;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.ts.generator.FromAliasGenerator;
import io.github.jinganix.webpb.ts.generator.Generator;
import io.github.jinganix.webpb.ts.generator.SubTypesGenerator;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.utils.ResultWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** The main class. */
public class Main {

  /**
   * main.
   *
   * @param args arguments
   * @throws Exception throws exception
   */
  public static void main(String[] args) throws Exception {
    RequestContext context = new RequestContext();
    Generator generator = Generator.create();
    Map<String, String> files = new HashMap<>();
    ResultWriter writer = new ResultWriter(System.out);
    for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
      String content = generator.generate(fileDescriptor);
      files.put(fileDescriptor.getPackage() + ".ts", content);
    }
    files.putAll(new SubTypesGenerator().generate(context.getTargetDescriptors()));
    files.putAll(new FromAliasGenerator().generate(context.getTargetDescriptors()));
    for (Entry<String, String> entry : files.entrySet()) {
      writer.write(entry.getKey(), entry.getValue());
    }
  }
}
