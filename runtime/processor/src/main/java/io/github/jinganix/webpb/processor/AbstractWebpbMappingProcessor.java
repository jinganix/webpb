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

package io.github.jinganix.webpb.processor;

import static javax.tools.Diagnostic.Kind.ERROR;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import io.github.jinganix.webpb.processor.misc.JavacProcessingEnvironments;
import io.github.jinganix.webpb.processor.misc.JvmOpens;
import io.github.jinganix.webpb.processor.misc.TreeMakerImport;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/** Shared javac AST rewriting for webpb Spring mapping annotations. */
abstract class AbstractWebpbMappingProcessor extends AbstractProcessor {

  static {
    JvmOpens.addOpens(AbstractWebpbMappingProcessor.class);
  }

  private final TreeMakerImport treeMakerImport = new TreeMakerImport();
  private Trees trees;
  private TreeMaker treeMaker;
  private Names names;

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of(
        "org.gradle.annotation.processing.incremental",
        "org.gradle.annotation.processing.aggregating");
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    JavacProcessingEnvironment env = JavacProcessingEnvironments.unwrap(processingEnv);
    if (env == null) {
      throw new RuntimeException("JavacProcessingEnvironment is required.");
    }
    this.trees = Trees.instance(env);
    this.treeMaker = TreeMaker.instance(env.getContext());
    this.names = Names.instance(env.getContext());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<JCCompilationUnit> units = new LinkedHashSet<>();
    for (TypeElement annotation : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
        TreePath path = trees.getPath(element);
        if (path != null) {
          units.add((JCCompilationUnit) path.getCompilationUnit());
        }
      }
    }
    for (JCCompilationUnit unit : units) {
      processUnit(unit);
    }
    return false;
  }

  protected abstract String sourceAnnotationName();

  protected abstract String missingMessageError();

  protected abstract JCAnnotation transformWebpbAnnotation(
      JCCompilationUnit unit, JCMethodDecl method, JCAnnotation annotation);

  protected TreeMaker treeMaker() {
    return treeMaker;
  }

  protected Names names() {
    return names;
  }

  protected void addImport(JCCompilationUnit unit, String packageName, String simpleName) {
    unit.defs = insertImport(unit.defs, packageName, simpleName);
  }

  protected JCAnnotation replaceAnnotation(
      JCCompilationUnit unit,
      JCMethodDecl method,
      JCAnnotation annotation,
      String targetSimpleName,
      String targetPackage,
      java.util.List<JCExpression> args) {
    if (!isAnnotation(annotation, sourceAnnotationName())) {
      return annotation;
    }
    addImport(unit, targetPackage, targetSimpleName);
    ClassSymbol messageSymbol = resolveMessageSymbol(method, annotation);
    if (messageSymbol == null) {
      logError(missingMessageError());
      return annotation;
    }
    ArrayList<JCExpression> mappingArgs = new ArrayList<>(args);
    enrichFromMessage(unit, messageSymbol, mappingArgs);
    return treeMaker.Annotation(
        treeMaker.Ident(names.fromString(targetSimpleName)), List.from(mappingArgs));
  }

  protected void enrichFromMessage(
      JCCompilationUnit unit, TypeSymbol messageSymbol, ArrayList<JCExpression> args) {
    // default: subclasses override
  }

  protected ClassSymbol resolveMessageSymbol(JCMethodDecl method, JCAnnotation annotation) {
    ClassSymbol messageSymbol = resolveMessageFromArgs(annotation);
    if (messageSymbol != null) {
      return messageSymbol;
    }
    return resolveMessageFromParameters(method);
  }

  protected ClassSymbol resolveMessageFromArgs(JCAnnotation annotation) {
    for (JCExpression arg : annotation.args) {
      if (!(arg instanceof JCTree.JCAssign assign)) {
        continue;
      }
      if (!(assign.lhs instanceof JCTree.JCIdent ident)) {
        continue;
      }
      if (!Const.MESSAGE_ATTRIBUTE.equals(ident.name.toString())) {
        continue;
      }
      return classSymbolFromType(assign.rhs);
    }
    return null;
  }

  protected ClassSymbol resolveMessageFromParameters(JCMethodDecl method) {
    for (JCTree.JCVariableDecl parameter : method.getParameters()) {
      TypeSymbol typeSymbol = parameter.sym.type.tsym;
      if (!(typeSymbol instanceof ClassSymbol classSymbol)) {
        continue;
      }
      for (Type type : classSymbol.getInterfaces()) {
        if (Const.WebpbMessage.equals(type.tsym.getQualifiedName().toString())) {
          return classSymbol;
        }
      }
    }
    return null;
  }

  protected ClassSymbol classSymbolFromType(JCExpression expression) {
    if (expression.type == null) {
      return null;
    }
    return expression.type.allparams().stream()
        .filter(type -> type.tsym instanceof ClassSymbol)
        .map(type -> (ClassSymbol) type.tsym)
        .findFirst()
        .orElse(null);
  }

  protected String webpbMappingPath(TypeSymbol messageSymbol) {
    for (Symbol element : messageSymbol.getEnclosedElements()) {
      if (!(element instanceof VarSymbol varSymbol)) {
        continue;
      }
      if (!Const.WEBPB_PATH.equals(varSymbol.getSimpleName().toString())) {
        continue;
      }
      Object value = varSymbol.getConstValue();
      if (value == null) {
        continue;
      }
      return value.toString().split("\\?")[0];
    }
    return null;
  }

  protected void copyPassthroughArgs(
      JCAnnotation annotation, ArrayList<JCExpression> args, String excludedAttribute) {
    for (JCExpression arg : annotation.args) {
      if (!(arg instanceof JCTree.JCAssign assign)) {
        continue;
      }
      if (!(assign.lhs instanceof JCTree.JCIdent ident)) {
        continue;
      }
      if (excludedAttribute.equals(ident.name.toString())) {
        continue;
      }
      args.add(
          treeMaker.Assign(treeMaker.Ident(names.fromString(ident.name.toString())), assign.rhs));
    }
  }

  private void processUnit(JCCompilationUnit unit) {
    for (JCTree def : unit.defs) {
      if (def.hasTag(JCTree.Tag.CLASSDEF)) {
        transformMethodsInType(unit, (JCClassDecl) def);
      }
    }
  }

  private void transformMethodsInType(JCCompilationUnit unit, JCClassDecl typeDecl) {
    for (JCTree member : typeDecl.defs) {
      if (member.hasTag(JCTree.Tag.METHODDEF)) {
        JCMethodDecl method = (JCMethodDecl) member;
        method.mods.annotations =
            List.from(
                method.mods.annotations.stream()
                    .map(annotation -> transformWebpbAnnotation(unit, method, annotation))
                    .collect(Collectors.toList()));
      }
      if (member.hasTag(JCTree.Tag.CLASSDEF)) {
        transformMethodsInType(unit, (JCClassDecl) member);
      }
    }
  }

  private boolean isAnnotation(JCAnnotation annotation, String qualifiedName) {
    if (annotation.annotationType.type == null) {
      return false;
    }
    return qualifiedName.equals(annotation.annotationType.type.toString());
  }

  private List<JCTree> insertImport(List<JCTree> defs, String packageName, String simpleName) {
    JCTree.JCImport importTree =
        treeMakerImport.Import(
            treeMaker,
            treeMaker.Select(
                treeMaker.Ident(names.fromString(packageName)), names.fromString(simpleName)),
            false);
    ArrayList<JCTree> updated = new ArrayList<>();
    for (JCTree tree : defs) {
      if (importTree != null && tree.hasTag(JCTree.Tag.CLASSDEF)) {
        updated.add(importTree);
        updated.add(tree);
        importTree = null;
      } else {
        updated.add(tree);
      }
    }
    return List.from(updated);
  }

  private void logError(String message) {
    processingEnv.getMessager().printMessage(ERROR, message);
  }
}
