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

package io.github.jinganix.webpb.processor.misc;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import java.lang.reflect.Field;
import javax.annotation.processing.ProcessingEnvironment;

/** Resolve {@link JavacProcessingEnvironment} from Gradle or IDE wrappers. */
public final class JavacProcessingEnvironments {

  private JavacProcessingEnvironments() {}

  /**
   * Unwrap a {@link ProcessingEnvironment} to {@link JavacProcessingEnvironment}.
   *
   * @param processingEnv annotation processing environment
   * @return javac environment, or {@code null} if unavailable
   */
  public static JavacProcessingEnvironment unwrap(ProcessingEnvironment processingEnv) {
    if (processingEnv instanceof JavacProcessingEnvironment javacProcessingEnvironment) {
      return javacProcessingEnvironment;
    }
    return unwrapDelegate(processingEnv);
  }

  private static JavacProcessingEnvironment unwrapDelegate(Object environment) {
    for (Class<?> type = environment.getClass(); type != null; type = type.getSuperclass()) {
      for (Field field : type.getDeclaredFields()) {
        JavacProcessingEnvironment javac = readJavacField(environment, field);
        if (javac != null) {
          return javac;
        }
      }
    }
    return null;
  }

  private static JavacProcessingEnvironment readJavacField(Object environment, Field field) {
    if (!JavacProcessingEnvironment.class.isAssignableFrom(field.getType())
        && !"delegate".equals(field.getName())
        && !"processingEnv".equals(field.getName())) {
      return null;
    }
    try {
      field.setAccessible(true);
      Object value = field.get(environment);
      if (value instanceof JavacProcessingEnvironment javacProcessingEnvironment) {
        return javacProcessingEnvironment;
      }
      if (value != null && value != environment) {
        return unwrapDelegate(value);
      }
    } catch (IllegalAccessException ignored) {
      // try next field
    }
    return null;
  }
}
