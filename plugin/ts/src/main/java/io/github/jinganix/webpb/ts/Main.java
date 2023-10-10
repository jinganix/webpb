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

package io.github.jinganix.webpb.ts;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.ts.generator.Generator;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.utils.ResultWriter;

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
    ResultWriter writer = new ResultWriter(System.out);
    for (FileDescriptor fileDescriptor : context.getTargetDescriptors()) {
      String content = generator.generate(fileDescriptor);
      writer.write(fileDescriptor.getPackage() + ".ts", content);
    }
  }
}
