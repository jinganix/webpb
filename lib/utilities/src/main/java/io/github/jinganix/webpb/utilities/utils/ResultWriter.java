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

package io.github.jinganix.webpb.utilities.utils;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import java.io.PrintStream;
import org.apache.commons.lang3.StringUtils;

/** Write generated result to system out. */
public class ResultWriter {

  private final PrintStream stream;

  /**
   * Constructor.
   *
   * @param stream {@link PrintStream}
   */
  public ResultWriter(PrintStream stream) {
    this.stream = stream;
  }

  /**
   * Write content with filename.
   *
   * @param filename file name
   * @param content file content
   */
  public void write(String filename, String content) {
    if (StringUtils.isEmpty(content)) {
      return;
    }
    CodeGeneratorResponse.Builder builder = CodeGeneratorResponse.newBuilder();
    builder.addFileBuilder().setName(filename).setContent(content);
    CodeGeneratorResponse response = builder.build();
    try {
      response.writeTo(this.stream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
