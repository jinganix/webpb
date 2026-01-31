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

package io.github.jinganix.webpb.java.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/** A {@link Name} with imported info. */
class ImportPath {

  private static final JavaParser JAVA_PARSER = new JavaParser();

  private final String path;

  private final String identifier;

  /**
   * Construct an {@link ImportPath}.
   *
   * @param path name to import
   */
  public ImportPath(String path) {
    Optional<Name> optional = JAVA_PARSER.parseName(path).getResult();
    if (!optional.isPresent()) {
      throw new RuntimeException("Invalid import path: " + path);
    }
    this.path = optional.get().asString();
    this.identifier = optional.get().getIdentifier();
  }

  /**
   * Resolve an {@link ImportPath} if {@link Name} is imported by this.
   *
   * @param name {@link Name} to resolve
   * @return {@link ImportPath}
   */
  public String relative(String name) {
    if (this.identifier.equals(name)) {
      return name;
    }
    Optional<Expression> optional = JAVA_PARSER.parseExpression(name).getResult();
    if (!optional.isPresent() || !(optional.get() instanceof FieldAccessExpr)) {
      return null;
    }
    FieldAccessExpr expr = optional.get().asFieldAccessExpr();
    List<SimpleName> nameList = new ArrayList<>();
    if (!match(expr, nameList)) {
      return null;
    }
    Collections.reverse(nameList);
    return nameList.stream().map(SimpleName::asString).collect(Collectors.joining("."));
  }

  private boolean match(FieldAccessExpr expr, List<SimpleName> nameList) {
    nameList.add(expr.getName());
    if (identifier.equals(expr.getNameAsString())) {
      return path.endsWith(expr.toString());
    }
    if (expr.getScope() instanceof FieldAccessExpr) {
      return match((FieldAccessExpr) expr.getScope(), nameList);
    }
    NameExpr nameExpr = (NameExpr) expr.getScope();
    if (identifier.equals(nameExpr.getName().toString())) {
      nameList.add(nameExpr.getName());
      return true;
    }
    return false;
  }

  /**
   * Get the imported path string.
   *
   * @return path string
   */
  public String getPath() {
    return path;
  }

  /**
   * Get identifier.
   *
   * @return identifier string
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * hashcode.
   *
   * @return hashcode
   */
  @Override
  public int hashCode() {
    return path.hashCode();
  }

  /**
   * equals.
   *
   * @param o {@link Object}
   * @return true
   */
  @Override
  public boolean equals(final Object o) {
    if (o instanceof ImportPath) {
      return StringUtils.equals(((ImportPath) o).getPath(), this.path);
    }
    return false;
  }
}
