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

import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;

import com.github.javaparser.JavaParser;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend;
import io.github.jinganix.webpb.utilities.utils.Const;
import io.github.jinganix.webpb.utilities.utils.DescriptorUtils;
import io.github.jinganix.webpb.utilities.utils.OptionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/** Utility to handle imports. */
public class Imports {

  private static final JavaParser JAVA_PARSER = new JavaParser();

  private final List<ImportPath> imported = new ArrayList<>();

  private final String javaPackage;

  private final List<ImportPath> lookup;

  private final ImportMapper importMapper;

  /**
   * Constructor.
   *
   * @param javaPackage java package
   * @param lookup list of {@link ImportPath}
   */
  public Imports(String javaPackage, List<ImportPath> lookup) {
    this.javaPackage = javaPackage;
    this.lookup = lookup;
    this.importMapper = new ImportMapper();
  }

  /**
   * Get lookup paths.
   *
   * @param fd {@link FileDescriptor}
   * @return list of {@link ImportPath}
   */
  public static List<ImportPath> getLookup(FileDescriptor fd) {
    return Stream.of(
            Arrays.asList(
                "java.lang.Integer",
                "java.lang.Long",
                "java.lang.String",
                "java.util.List",
                "java.util.Map",
                Const.RUNTIME_PACKAGE + ".WebpbMessage",
                Const.RUNTIME_PACKAGE + ".WebpbMeta",
                Const.RUNTIME_PACKAGE + ".WebpbSubValue",
                Const.RUNTIME_PACKAGE + ".common.InQuery",
                Const.RUNTIME_PACKAGE + ".enumeration.Enumeration"),
            OptionUtils.getWebpbOpts(fd, WebpbExtend.FileOpts::hasJava).getJava().getImportList(),
            getOpts(fd, WebpbExtend.FileOpts::hasJava).getJava().getImportList(),
            DescriptorUtils.resolveTopLevelTypes(fd).stream()
                .map(
                    e ->
                        StringUtils.isEmpty(GeneratorUtils.getJavaPackage(e))
                            ? e.getName()
                            : GeneratorUtils.getJavaPackage(e) + "." + e.getName())
                .collect(Collectors.toList()))
        .flatMap(List::stream)
        .map(ImportPath::new)
        .sorted(((o1, o2) -> Integer.compare(o2.getPath().length(), o1.getPath().length())))
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Import an annotation.
   *
   * @param str annotation, eg: @Anno("abc")
   * @return imported annotation
   */
  public String importAnnotation(String str) {
    return JAVA_PARSER
        .parseAnnotation(str)
        .getResult()
        .map(
            expr -> {
              expr.accept(new ImportVisitor(), this);
              return expr.toString();
            })
        .orElseThrow(() -> new RuntimeException("Bad annotation: " + str));
  }

  /**
   * Import a class or interface.
   *
   * @param str a class or interface, eg: List&lt;String&gt;
   * @return imported class or interface
   */
  public String importClassOrInterface(String str) {
    return JAVA_PARSER
        .parseClassOrInterfaceType(str)
        .getResult()
        .map(
            expr -> {
              expr.accept(new ImportVisitor(), this);
              return expr.toString();
            })
        .orElseThrow(() -> new RuntimeException("Bad class or interface: " + str));
  }

  /**
   * Import from an enum or a message descriptor.
   *
   * @param descriptor {@link GenericDescriptor}
   * @return imported string
   */
  public String importGenericDescriptor(GenericDescriptor descriptor) {
    FileDescriptor fd = descriptor.getFile();
    String packageName = fd.getPackage();
    String relative = descriptor.getFullName().replace(packageName + ".", "");
    String prefix =
        StringUtils.isEmpty(GeneratorUtils.getJavaPackage(fd))
            ? ""
            : GeneratorUtils.getJavaPackage(fd) + ".";
    String fullPath = prefix + relative;
    String importPath = prefix + relative.split("\\.")[0];
    return checkAndImport(fullPath, importPath, relative);
  }

  /**
   * Find qualified name in imported paths.
   *
   * @param name qualified or simple name
   * @return qualified name or itself
   */
  public String importedQualifiedName(String name) {
    for (ImportPath importPath : imported) {
      String relative = importPath.relative(name);
      if (relative != null) {
        return importPath.getPath();
      }
    }
    return name;
  }

  /**
   * Import a name (eg. foo.bar) or simple name (eg. foo).
   *
   * @param name a name or simple name
   * @return imported name eg. name a.b.c -> return c
   */
  public String importName(String name) {
    for (ImportPath importPath : lookup) {
      String relative = importPath.relative(name);
      if (relative != null) {
        return checkAndImport(name, importPath.getPath(), relative);
      }
    }
    throw new RuntimeException("No import path found for: " + name);
  }

  private String checkAndImport(String fullPath, String importPath, String relative) {
    String mapped = importMapper.map(importPath);
    int index = mapped.lastIndexOf(".");
    String importPackage = index < 0 ? mapped : mapped.substring(0, index);
    if (importPackage.equals(this.javaPackage) || importPackage.equals(importPath)) {
      return relative;
    }
    String identifier = relative.split("\\.")[0];
    for (ImportPath imported : imported) {
      if (!imported.getPath().equals(mapped) && imported.getIdentifier().equals(identifier)) {
        return importMapper.map(fullPath);
      }
    }
    if (imported.stream().noneMatch(e -> e.getPath().equals(mapped))) {
      imported.add(new ImportPath(mapped));
    }
    return relative;
  }

  /**
   * To imported path strings.
   *
   * @return imported paths
   */
  public List<String> toList() {
    return imported.stream()
        .map(e -> importMapper.map(e.getPath()))
        .filter(StringUtils::isNotEmpty)
        .distinct()
        .sorted(StringUtils::compare)
        .collect(Collectors.toList());
  }
}
