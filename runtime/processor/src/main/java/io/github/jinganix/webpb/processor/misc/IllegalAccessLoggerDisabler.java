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

import java.lang.reflect.Field;

/**
 * Suppresses JDK illegal reflective access warnings during annotation processing.
 *
 * <p>Modeled after {@code lombok.launch.AnnotationProcessorHider.AnnotationProcessor}.
 */
public final class IllegalAccessLoggerDisabler {

  private IllegalAccessLoggerDisabler() {}

  @SuppressWarnings("sunapi")
  public static void disable() {
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      Object unsafe = theUnsafe.get(null);

      Class<?> loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
      Field logger = loggerClass.getDeclaredField("logger");
      MethodHandles.putObjectVolatile(unsafe, unsafeClass, loggerClass, logger, null);
    } catch (Throwable ignored) {
      // ignored
    }
  }

  private static final class MethodHandles {

    private MethodHandles() {}

    static void putObjectVolatile(
        Object unsafe, Class<?> unsafeClass, Class<?> holder, Field field, Object value)
        throws ReflectiveOperationException {
      var staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);
      staticFieldOffset.setAccessible(true);
      long offset = (long) staticFieldOffset.invoke(unsafe, field);
      var putObjectVolatile =
          unsafeClass.getDeclaredMethod(
              "putObjectVolatile", Object.class, long.class, Object.class);
      putObjectVolatile.setAccessible(true);
      putObjectVolatile.invoke(unsafe, holder, offset, value);
    }
  }
}
