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

package io.github.jinganix.webpb.java.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

/** Mapping imported path to another. */
public class ImportMapper {

  private final List<Mapping> mappings = new ArrayList<>();

  /** Constructor. */
  public ImportMapper() {
    mappings.add(new Mapping("^java.lang.*$", ""));
    mappings.add(new Mapping("^com.google.protobuf.Any$", "io.github.jinganix.webpb.runtime.Any"));
  }

  /**
   * Map a name to another.
   *
   * @param name name to map
   * @return mapped name
   */
  public String map(String name) {
    for (Mapping mapping : mappings) {
      Matcher matcher = mapping.getPattern().matcher(name);
      if (matcher.find()) {
        return matcher.replaceFirst(mapping.getReplacement());
      }
    }
    return name;
  }

  /** Mapping. */
  @Getter
  private static class Mapping {

    private final Pattern pattern;

    private final String replacement;

    /**
     * Constructor.
     *
     * @param pattern pattern to match
     * @param replacement replacement
     */
    public Mapping(String pattern, String replacement) {
      this.pattern = Pattern.compile(pattern);
      this.replacement = replacement;
    }
  }
}
