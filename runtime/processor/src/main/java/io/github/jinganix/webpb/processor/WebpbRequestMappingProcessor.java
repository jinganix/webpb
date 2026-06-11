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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import java.util.ArrayList;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

/** Process the WebpbRequestMapping and transform it to Spring RequestMapping. */
@SupportedAnnotationTypes(Const.WebpbRequestMapping)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class WebpbRequestMappingProcessor extends AbstractWebpbMappingProcessor {

  private static final String SPRING_WEB_BIND = "org.springframework.web.bind.annotation";

  @Override
  protected String sourceAnnotationName() {
    return Const.WebpbRequestMapping;
  }

  @Override
  protected String missingMessageError() {
    return "Should specify a message for WebpbRequestMapping";
  }

  @Override
  protected JCAnnotation transformWebpbAnnotation(
      JCCompilationUnit unit, JCMethodDecl method, JCAnnotation annotation) {
    ArrayList<JCExpression> args = new ArrayList<>();
    copyPassthroughArgs(annotation, args, Const.MESSAGE_ATTRIBUTE);
    return replaceAnnotation(unit, method, annotation, "RequestMapping", SPRING_WEB_BIND, args);
  }

  @Override
  protected void enrichFromMessage(
      JCCompilationUnit unit, TypeSymbol messageSymbol, ArrayList<JCExpression> args) {
    for (Symbol element : messageSymbol.getEnclosedElements()) {
      if (!(element instanceof VarSymbol varSymbol)) {
        continue;
      }
      if (Const.WEBPB_METHOD.equals(varSymbol.getSimpleName().toString())) {
        Object method = varSymbol.getConstValue();
        if (method != null) {
          args.add(
              treeMaker()
                  .Assign(
                      treeMaker().Ident(names().fromString("method")),
                      treeMaker()
                          .Select(
                              treeMaker().Ident(names().fromString("RequestMethod")),
                              names().fromString(method.toString()))));
          addImport(unit, SPRING_WEB_BIND, "RequestMethod");
        }
      }
    }
    String path = webpbMappingPath(messageSymbol);
    if (path != null) {
      args.add(
          treeMaker()
              .Assign(treeMaker().Ident(names().fromString("path")), treeMaker().Literal(path)));
    }
  }
}
