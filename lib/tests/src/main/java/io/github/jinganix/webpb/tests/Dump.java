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
  /** Proto2 alias skip fixture. */
  proto2_alias_skip,
  /** Proto2 auto alias fixture. */
  proto2_auto_alias,
  /** Proto2 core codegen fixture. */
  proto2_core_codegen,
  /** Proto2 enumeration fixture. */
  proto2_enumeration,
  /** Proto2 errors fixture. */
  proto2_errors,
  /** Proto2 generator options fixture. */
  proto2_generator_options,
  /** Proto2 message extends fixture. */
  proto2_message_extends,
  /** Proto2 imports fixture. */
  proto2_imports,
  /** Proto3 alias skip fixture. */
  proto3_alias_skip,
  /** Proto3 auto alias fixture. */
  proto3_auto_alias,
  /** Proto3 core codegen fixture. */
  proto3_core_codegen,
  /** Proto3 enumeration fixture. */
  proto3_enumeration,
  /** Proto3 errors fixture. */
  proto3_errors,
  /** Proto3 generator options fixture. */
  proto3_generator_options,
  /** Proto3 message extends fixture. */
  proto3_message_extends,
  /** Proto3 imports fixture. */
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
