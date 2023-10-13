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

package io.github.jinganix.webpb.java.generator;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.java.utils.GeneratorUtils;
import io.github.jinganix.webpb.java.utils.Imports;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumValueOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.JavaEnumOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptEnumValueOpts;
import io.github.jinganix.webpb.utilities.utils.OptionUtils;
import io.github.jinganix.webpb.utilities.utils.Templates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/** Generator for enum definition. */
public class EnumGenerator {

  private final Templates templates = new Templates();

  private final FileDescriptor fileDescriptor;

  private final Imports imports;

  /**
   * Constructor.
   *
   * @param fileDescriptor {@link FileDescriptor}
   */
  public EnumGenerator(FileDescriptor fileDescriptor) {
    this.fileDescriptor = fileDescriptor;
    this.imports = new Imports(fileDescriptor);
  }

  /**
   * Generate enum declaration.
   *
   * @param enumDescriptor {@link EnumDescriptor}
   * @return {@link String}
   */
  public String generate(EnumDescriptor enumDescriptor) {
    Map<String, Object> data = new HashMap<>();
    data.put("filename", enumDescriptor.getFile().getName());
    data.put("package", GeneratorUtils.getJavaPackage(this.fileDescriptor));
    data.put("msgAnnos", getAnnotations(enumDescriptor));
    data.put("className", enumDescriptor.getName());
    data.put("implements", getImplements(enumDescriptor));
    data.put("enums", getEnums(enumDescriptor));
    data.put("valueType", getValueType(enumDescriptor));
    data.put("imports", imports.toList());

    return templates.process("enum.ftl", data);
  }

  private String getValueType(EnumDescriptor enumDescriptor) {
    return OptionUtils.isStringValue(enumDescriptor) ? "String" : "Integer";
  }

  private List<String> getAnnotations(EnumDescriptor enumDescriptor) {
    JavaEnumOpts enumOpts = OptionUtils.getOpts(enumDescriptor, EnumOpts::hasJava).getJava();
    return enumOpts.getAnnotationList().stream()
        .map(imports::importAnnotation)
        .collect(Collectors.toList());
  }

  private List<String> getImplements(EnumDescriptor enumDescriptor) {
    JavaEnumOpts enumOpts = OptionUtils.getOpts(enumDescriptor, EnumOpts::hasJava).getJava();
    List<String> values = new ArrayList<>();
    values.add(imports.importClassOrInterface("Enumeration<" + getValueType(enumDescriptor) + ">"));
    for (String impl : enumOpts.getImplementsList()) {
      values.add(imports.importClassOrInterface(impl));
    }
    return values;
  }

  private List<Map<String, String>> getEnums(EnumDescriptor enumDescriptor) {
    List<Map<String, String>> enums = new ArrayList<>();
    for (EnumValueDescriptor descriptor : enumDescriptor.getValues()) {
      Map<String, String> data = new HashMap<>();
      data.put("name", descriptor.getName());
      data.put("value", getEnumValue(enumDescriptor, descriptor));
      enums.add(data);
    }
    return enums;
  }

  private String getEnumValue(EnumDescriptor enumDescriptor, EnumValueDescriptor descriptor) {
    OptEnumValueOpts opts = OptionUtils.getOpts(descriptor, EnumValueOpts::hasOpt).getOpt();
    if (StringUtils.isEmpty(opts.getValue())) {
      if (OptionUtils.isStringValue(enumDescriptor)) {
        return "\"" + descriptor.getName() + "\"";
      } else {
        return String.valueOf(descriptor.getNumber());
      }
    }
    return "\"" + opts.getValue() + "\"";
  }
}
