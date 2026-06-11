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

import java.lang.reflect.Method;

/** Utilities to open JVM modules for javac internals. */
public final class JvmOpens {

  private static final String[] PACKAGES = {
    "com.sun.tools.javac.code",
    "com.sun.tools.javac.comp",
    "com.sun.tools.javac.file",
    "com.sun.tools.javac.main",
    "com.sun.tools.javac.model",
    "com.sun.tools.javac.parser",
    "com.sun.tools.javac.processing",
    "com.sun.tools.javac.tree",
    "com.sun.tools.javac.util",
    "com.sun.tools.javac.jvm",
  };

  private JvmOpens() {}

  /**
   * Open javac internals to the module of {@code anchor}.
   *
   * <p>Modeled after {@code lombok.javac.apt.LombokProcessor#addOpensForLombok()}.
   *
   * @param anchor class whose module receives the opens
   */
  public static void addOpens(Class<?> anchor) {
    Class<?> moduleClass;
    try {
      moduleClass = Class.forName("java.lang.Module");
    } catch (ClassNotFoundException e) {
      return;
    }

    Object jdkCompilerModule = getJdkCompilerModule();
    Object ownModule = getOwnModule(anchor);
    if (jdkCompilerModule == null || ownModule == null) {
      return;
    }

    try {
      Method implAddOpens =
          Permit.getMethod(moduleClass, "implAddOpens", String.class, moduleClass);
      for (String pkg : PACKAGES) {
        implAddOpens.invoke(jdkCompilerModule, pkg, ownModule);
      }
    } catch (Exception ignored) {
      // ignored
    }
  }

  private static Object getOwnModule(Class<?> anchor) {
    try {
      return Permit.getMethod(Class.class, "getModule").invoke(anchor);
    } catch (Exception e) {
      return null;
    }
  }

  private static Object getJdkCompilerModule() {
    try {
      Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
      Method boot = moduleLayerClass.getDeclaredMethod("boot");
      Object bootLayer = boot.invoke(null);
      Class<?> optionalClass = Class.forName("java.util.Optional");
      Method findModule = moduleLayerClass.getDeclaredMethod("findModule", String.class);
      Object compiler = findModule.invoke(bootLayer, "jdk.compiler");
      return optionalClass.getDeclaredMethod("get").invoke(compiler);
    } catch (Exception e) {
      return null;
    }
  }
}
