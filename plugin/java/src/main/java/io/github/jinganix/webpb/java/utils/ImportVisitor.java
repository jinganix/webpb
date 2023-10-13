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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/** Visitor to visit parsed node and do import. */
public class ImportVisitor extends VoidVisitorAdapter<Imports> {

  private static final JavaParser JAVA_PARSER = new JavaParser();

  /**
   * Visit a {@link ClassOrInterfaceType}.
   *
   * @param n {@link ClassOrInterfaceType}
   * @param arg {@link Imports}
   */
  @Override
  public void visit(ClassOrInterfaceType n, Imports arg) {
    Name scope = n.getScope().map(e -> new Name(e.asString())).orElse(null);
    Name typeName = new Name(scope, n.getNameAsString());
    String imported = arg.importName(typeName.asString());
    if (!imported.equals(n.asString())) {
      n.removeScope();
      n.setName(imported);
    }
  }

  /**
   * Visit a {@link MemberValuePair}.
   *
   * @param n {@link MemberValuePair}
   * @param arg {@link Imports}
   */
  @Override
  public void visit(MemberValuePair n, Imports arg) {
    if (n.getValue().isFieldAccessExpr()) {
      String member = n.getValue().toString();
      String imported = arg.importName(member);
      if (!imported.equals(member)) {
        JAVA_PARSER.parseExpression(imported).ifSuccessful(n::setValue);
      }
      n.getName().accept(this, arg);
    } else {
      super.visit(n, arg);
    }
  }

  /**
   * Visit a {@link Name}.
   *
   * @param n {@link Name}
   * @param arg {@link Imports}
   */
  @Override
  public void visit(Name n, Imports arg) {
    String imported = arg.importName(n.asString());
    if (imported.equals(n.asString())) {
      return;
    }
    n.removeQualifier();
    n.setIdentifier(imported);
  }

  /**
   * Visit a {@link SingleMemberAnnotationExpr}.
   *
   * @param n {@link SingleMemberAnnotationExpr}
   * @param arg {@link Imports}
   */
  @Override
  public void visit(SingleMemberAnnotationExpr n, Imports arg) {
    if (n.getMemberValue().isFieldAccessExpr()) {
      String member = n.getMemberValue().toString();
      String imported = arg.importName(member);
      if (!imported.equals(member)) {
        JAVA_PARSER.parseExpression(imported).ifSuccessful(n::setMemberValue);
      }
    }
    n.getName().accept(this, arg);
  }
}
