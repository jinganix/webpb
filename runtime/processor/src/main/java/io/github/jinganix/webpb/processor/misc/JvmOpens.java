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

import com.sun.tools.javac.tree.TreeMaker;

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
   * Add opens for specified class when javac internals are not already accessible.
   *
   * @param type target class type
   */
  public static void addOpens(Class<?> type) {
    if (isJavacTreeAccessible()) {
      return;
    }

    Class<?> moduleClass;
    try {
      moduleClass = Class.forName("java.lang.Module");
    } catch (ClassNotFoundException e) {
      return;
    }

    Object jdkCompilerModule = getJdkCompilerModule();
    Object ownModule = getOwnModule(type);
    if (jdkCompilerModule == null || ownModule == null) {
      return;
    }

    try {
      var addOpens = Permit.getMethod(moduleClass, "implAddOpens", String.class, moduleClass);
      for (String pkg : PACKAGES) {
        addOpens.invoke(jdkCompilerModule, pkg, ownModule);
      }
    } catch (Exception ignored) {
      // ignored
    }
  }

  private static boolean isJavacTreeAccessible() {
    try {
      TreeMaker.class.getDeclaredMethods();
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private static Object getJdkCompilerModule() {
    try {
      Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
      var boot = moduleLayerClass.getDeclaredMethod("boot");
      boot.setAccessible(true);
      Object bootLayer = boot.invoke(null);
      var findModule = moduleLayerClass.getDeclaredMethod("findModule", String.class);
      findModule.setAccessible(true);
      Object compiler = findModule.invoke(bootLayer, "jdk.compiler");
      Class<?> optionalClass = Class.forName("java.util.Optional");
      return optionalClass.getDeclaredMethod("get").invoke(compiler);
    } catch (Exception e) {
      return null;
    }
  }

  private static Object getOwnModule(Class<?> type) {
    try {
      var getModule = Permit.getMethod(Class.class, "getModule");
      return getModule.invoke(type);
    } catch (Exception e) {
      return null;
    }
  }
}
