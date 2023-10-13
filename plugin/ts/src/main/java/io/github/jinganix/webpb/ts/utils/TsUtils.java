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

package io.github.jinganix.webpb.ts.utils;

import org.apache.commons.lang3.StringUtils;

/** Utilities. */
public class TsUtils {

  private TsUtils() {}

  /**
   * Convert to interface name.
   *
   * @param name name to convert
   * @return interface name start with I
   */
  public static String toInterfaceName(String name) {
    if (StringUtils.isEmpty(name)) {
      return name;
    }
    int lastIndex = name.lastIndexOf(".");
    if (lastIndex < 0) {
      return "I" + name;
    }
    return name.substring(0, lastIndex) + ".I" + name.substring(lastIndex + 1);
  }
}
