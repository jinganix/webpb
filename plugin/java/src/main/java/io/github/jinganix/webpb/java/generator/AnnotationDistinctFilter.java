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

package io.github.jinganix.webpb.java.generator;

import com.github.javaparser.JavaParser;
import io.github.jinganix.webpb.java.utils.Imports;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Filter distinct annotation if it is not repeatable. */
public class AnnotationDistinctFilter implements Predicate<String> {

  private static final JavaParser JAVA_PARSER = new JavaParser();

  private final Imports imports;

  private final List<String> repeatable;

  private final Set<String> existingSet = new HashSet<>();

  /**
   * Constructor.
   *
   * @param imports {@link Imports}
   * @param repeatable repeatable annotations
   */
  public AnnotationDistinctFilter(Imports imports, List<String> repeatable) {
    this.imports = imports;
    this.repeatable = repeatable;
  }

  /**
   * whether an annotation is filtered
   *
   * @param str annotation
   * @return true if filtered
   */
  @Override
  public boolean test(String str) {
    return JAVA_PARSER
        .parseAnnotation(str)
        .getResult()
        .map(
            expr -> {
              String name = imports.importedQualifiedName(expr.getNameAsString());
              if (repeatable.contains(name)) {
                return true;
              }
              if (existingSet.contains(name)) {
                return false;
              }
              existingSet.add(name);
              return true;
            })
        .orElseThrow(() -> new RuntimeException("Bad annotation: " + str));
  }
}
