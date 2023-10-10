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

package io.github.jinganix.webpb.java.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.protobuf.Descriptors;
import java.util.List;
import java.util.Objects;

/** GeneratorUtils. */
public class GeneratorUtils {

  private static final JavaParser JAVA_PARSER = new JavaParser();

  private GeneratorUtils() {}

  /**
   * Check whether annotation exists in annotations.
   *
   * @param annotations list of annotations
   * @param annotation annotation
   * @return true if exists
   */
  public static boolean exists(List<String> annotations, String annotation) {
    AnnotationExpr expr =
        JAVA_PARSER
            .parseAnnotation(annotation)
            .getResult()
            .orElseThrow(() -> new RuntimeException("Bad annotation:" + annotation));
    for (String anno : annotations) {
      AnnotationExpr iter =
          JAVA_PARSER
              .parseAnnotation(anno)
              .getResult()
              .orElseThrow(() -> new RuntimeException("Bad annotation:" + anno));
      if (Objects.equals(iter.getName(), expr.getName())) {
        return true;
      }
    }
    return false;
  }

  public static String getJavaPackage(Descriptors.GenericDescriptor descriptor) {
    return descriptor.getFile().getOptions().getJavaPackage();
  }
}
