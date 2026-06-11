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

package io.github.jinganix.webpb.tests;

import java.io.InputStream;

/** Utilities to handle test dump files. */
public enum Dump {
  proto2_alias_skip,
  proto2_auto_alias,
  proto2_core_codegen,
  proto2_enumeration,
  proto2_errors,
  proto2_generator_options,
  proto2_message_extends,
  proto2_imports,
  proto3_alias_skip,
  proto3_auto_alias,
  proto3_core_codegen,
  proto3_enumeration,
  proto3_errors,
  proto3_generator_options,
  proto3_message_extends,
  proto3_imports;

  private final String dumpName;

  Dump() {
    this.dumpName = String.format("/%s/dump/test.dump", this.name());
  }

  /** Load dump file. */
  public void pipe() {
    InputStream inputStream = getClass().getResourceAsStream(dumpName);
    System.setIn(inputStream);
  }
}
