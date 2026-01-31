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

package io.github.jinganix.webpb.ts.utils;

import lombok.Getter;

/** ImportPath. */
@Getter
public class ImportPath {

  private final String name;

  private final String path;

  private int order;

  /**
   * Constructor.
   *
   * @param name as name
   * @param path import path
   */
  public ImportPath(String name, String path) {
    this.name = name;
    this.path = path;
  }

  /**
   * Constructor.
   *
   * @param name as name
   * @param path import path
   * @param order import soring order
   */
  public ImportPath(String name, String path, int order) {
    this.name = name;
    this.path = path;
    this.order = order;
  }
}
