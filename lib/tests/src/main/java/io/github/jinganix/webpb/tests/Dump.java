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

package io.github.jinganix.webpb.tests;

import java.io.InputStream;

/** Utilities to handle test dump files. */
public enum Dump {
  auto_alias,
  enumeration,
  error_test,
  extends_test,
  import_test,
  test1,
  test2,
  test3;

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
