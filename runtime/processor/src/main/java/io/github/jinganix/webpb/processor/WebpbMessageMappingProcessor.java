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

import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import java.util.ArrayList;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

/** Process the WebpbMessageMapping and transform it to Spring MessageMapping. */
@SupportedAnnotationTypes(Const.WebpbMessageMapping)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class WebpbMessageMappingProcessor extends AbstractWebpbMappingProcessor {

  private static final String SPRING_MESSAGING = "org.springframework.messaging.handler.annotation";

  @Override
  protected String sourceAnnotationName() {
    return Const.WebpbMessageMapping;
  }

  @Override
  protected String missingMessageError() {
    return "Should specify a message for WebpbMessageMapping";
  }

  @Override
  protected JCAnnotation transformWebpbAnnotation(
      JCCompilationUnit unit, JCMethodDecl method, JCAnnotation annotation) {
    return replaceAnnotation(
        unit, method, annotation, "MessageMapping", SPRING_MESSAGING, new ArrayList<>());
  }

  @Override
  protected void enrichFromMessage(
      JCCompilationUnit unit, TypeSymbol messageSymbol, ArrayList<JCExpression> args) {
    String path = webpbMappingPath(messageSymbol);
    if (path != null) {
      args.add(treeMaker().Literal(path));
    }
  }
}
