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

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BOOL;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.DOUBLE;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FIXED32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FIXED64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SFIXED32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SFIXED64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.UINT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.UINT64;
import static io.github.jinganix.webpb.java.utils.GeneratorUtils.getJavaPackage;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getGenericDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapKeyDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapValueDescriptor;
import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;
import static java.util.Collections.singletonList;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.java.utils.GeneratorUtils;
import io.github.jinganix.webpb.java.utils.Imports;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FieldOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FileOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.JavaFileOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptFieldOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import io.github.jinganix.webpb.utilities.utils.AliasUtils;
import io.github.jinganix.webpb.utilities.utils.Const;
import io.github.jinganix.webpb.utilities.utils.DescriptorUtils;
import io.github.jinganix.webpb.utilities.utils.OptionUtils;
import io.github.jinganix.webpb.utilities.utils.Templates;
import io.github.jinganix.webpb.utilities.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/** Generator for {@link Descriptor}. */
public class MessageGenerator {

  private static final Map<FieldDescriptor.Type, String> TYPES;

  static {
    Map<FieldDescriptor.Type, String> map = new HashMap<>();
    map.put(BOOL, Boolean.class.getSimpleName());
    map.put(BYTES, "byte[]");
    map.put(DOUBLE, Double.class.getSimpleName());
    map.put(FIXED32, Integer.class.getSimpleName());
    map.put(FIXED64, Long.class.getSimpleName());
    map.put(FLOAT, Float.class.getSimpleName());
    map.put(INT32, Integer.class.getSimpleName());
    map.put(INT64, Long.class.getSimpleName());
    map.put(SFIXED32, Integer.class.getSimpleName());
    map.put(SFIXED64, Long.class.getSimpleName());
    map.put(SINT32, Integer.class.getSimpleName());
    map.put(SINT64, Long.class.getSimpleName());
    map.put(STRING, String.class.getSimpleName());
    map.put(UINT32, Integer.class.getSimpleName());
    map.put(UINT64, Long.class.getSimpleName());
    TYPES = map;
  }

  private final Templates templates = new Templates();

  private final FileDescriptor fileDescriptor;

  private final Imports imports;

  private final JavaFileOpts webpbOpts;

  private final JavaFileOpts fileOpts;

  /**
   * Constructor.
   *
   * @param fileDescriptor {@link FileDescriptor}
   */
  public MessageGenerator(FileDescriptor fileDescriptor) {
    this.fileDescriptor = fileDescriptor;
    this.imports = new Imports(getJavaPackage(fileDescriptor), Imports.getLookup(fileDescriptor));
    this.webpbOpts = OptionUtils.getWebpbOpts(fileDescriptor, FileOpts::hasJava).getJava();
    this.fileOpts = OptionUtils.getOpts(fileDescriptor, FileOpts::hasJava).getJava();
  }

  /**
   * Entrance of the generator.
   *
   * @param descriptor {@link Descriptor}
   * @return generated file content
   */
  public String generate(Descriptor descriptor) {
    return generate(
        () -> {
          OptionUtils.checkDuplicatedFields(descriptor);
          Map<String, Object> data = getMessageData(descriptor, 0);
          data.put("filename", descriptor.getFile().getName());
          data.put("package", GeneratorUtils.getJavaPackage(this.fileDescriptor));
          data.put("imports", imports.toList());
          return data;
        },
        "message.ftl",
        0);
  }

  private String generate(Supplier<Map<String, Object>> supplier, String ftl, int level) {
    String content = templates.process(ftl, supplier.get());
    Pattern pattern = Pattern.compile("^(.+)$", Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    return matcher.replaceAll(indent(level) + "$1");
  }

  private String indent(int level) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < level * 2; i++) {
      builder.append(' ');
    }
    return builder.toString();
  }

  private Map<String, Object> getMessageData(Descriptor descriptor, int level) {
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    Map<String, Object> data = new HashMap<>();
    data.put("msgAnnos", getMessageAnnotations(descriptor));
    data.put("className", descriptor.getName());
    data.put("extend", getExtend(descriptor));
    data.put("implements", getImplements(descriptor));
    data.put("method", opts.getMethod());
    data.put("context", Utils.normalize(opts.getContext()));
    data.put("path", Utils.normalize(opts.getPath()));
    data.put("webpbMeta", imports.importClassOrInterface("WebpbMeta"));
    data.put("fields", getFields(descriptor));
    data.put("nestedMsgs", getNestedMessages(descriptor, level + 1));
    data.put("genSetter", webpbOpts.getGenSetter());
    data.put("genGetter", webpbOpts.getGenGetter());
    return data;
  }

  private List<String> getMessageAnnotations(Descriptor descriptor) {
    return Stream.of(
            getOpts(descriptor, MessageOpts::hasJava).getJava().getAnnotationList(),
            fileOpts.getAnnotationList(),
            webpbOpts.getAnnotationList(),
            getSubValueAnnotations(descriptor))
        .flatMap(List::stream)
        .map(imports::importAnnotation)
        .filter(new AnnotationDistinctFilter(imports, webpbOpts.getRepeatableAnnotationList()))
        .collect(Collectors.toList());
  }

  private List<String> getSubValueAnnotations(Descriptor descriptor) {
    return getSubValues(descriptor).stream()
        .map(
            subValue -> {
              if (!subValue.contains(".")) {
                return singletonList("@WebpbSubValue(\"" + subValue + "\")");
              }
              EnumValueDescriptor valueDescriptor =
                  DescriptorUtils.resolveEnumValue(singletonList(descriptor.getFile()), subValue);
              if (valueDescriptor == null) {
                return singletonList("@WebpbSubValue(\"" + subValue + "\")");
              }
              return Arrays.asList(
                  "@WebpbSubValue(\"" + valueDescriptor.getNumber() + "\")",
                  "@WebpbSubValue(\"" + valueDescriptor.getName() + "\")");
            })
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private String getExtend(Descriptor descriptor) {
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    if (StringUtils.isNotEmpty(opts.getExtends())) {
      return DescriptorUtils.resolveTopLevelTypes(descriptor.getFile()).stream()
          .map(DescriptorUtils::resolveNestedTypes)
          .flatMap(List::stream)
          .filter(d -> d.getFullName().equals(opts.getExtends()))
          .findFirst()
          .map(imports::importGenericDescriptor)
          .orElseGet(() -> imports.importClassOrInterface(opts.getExtends()));
    }
    return null;
  }

  private List<String> getImplements(Descriptor descriptor) {
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    List<String> values = new ArrayList<>();
    values.add(imports.importClassOrInterface("WebpbMessage"));
    for (String impl : opts.getImplementsList()) {
      values.add(imports.importClassOrInterface(impl));
    }
    return values;
  }

  private List<Map<String, Object>> getFields(Descriptor descriptor) {
    Map<String, String> autoAliases = AliasUtils.getAutoAliases(descriptor);
    List<FieldDescriptor> fieldDescriptors = getMemberFields(descriptor);
    List<Map<String, Object>> fields = new ArrayList<>();
    for (FieldDescriptor field : fieldDescriptors) {
      Map<String, Object> data = new HashMap<>();
      data.put("type", getFieldType(field));
      data.put("name", field.getName());
      data.put("annos", getFieldAnnotations(descriptor, field, autoAliases.get(field.getName())));
      fields.add(data);
    }
    return fields;
  }

  private List<FieldDescriptor> getMemberFields(Descriptor descriptor) {
    return descriptor.getFields().stream()
        .filter(
            fieldDescriptor -> !getOpts(fieldDescriptor, FieldOpts::hasOpt).getOpt().getOmitted())
        .collect(Collectors.toList());
  }

  private String getFieldType(FieldDescriptor field) {
    if (field.isMapField()) {
      FieldDescriptor key = getMapKeyDescriptor(field);
      FieldDescriptor value = getMapValueDescriptor(field);
      return imports.importClassOrInterface("Map<" + toType(key) + ", " + toType(value) + ">");
    } else if (field.isRepeated()) {
      return imports.importClassOrInterface("List<" + toType(field) + ">");
    }
    return toType(field);
  }

  private List<String> getFieldAnnotations(
      Descriptor descriptor, FieldDescriptor field, String alias) {
    List<String> annotations =
        Stream.of(
                getOpts(field, FieldOpts::hasJava).getJava().getAnnotationList(),
                getOpts(descriptor, MessageOpts::hasJava).getJava().getFieldAnnotationList(),
                fileOpts.getFieldAnnotationList(),
                webpbOpts.getFieldAnnotationList())
            .flatMap(List::stream)
            .map(x -> x.replace("{{_ALIAS_}}", alias))
            .map(x -> x.replace("{{_FIELD_NAME_}}", field.getName()))
            .collect(Collectors.toList());
    OptFieldOpts optFieldOpts = getOpts(field, FieldOpts::hasOpt).getOpt();
    if (optFieldOpts.getInQuery()) {
      annotations.add("@" + Const.RUNTIME_PACKAGE + ".common.InQuery");
    }
    return annotations.stream()
        .map(imports::importAnnotation)
        .filter(new AnnotationDistinctFilter(imports, webpbOpts.getRepeatableAnnotationList()))
        .collect(Collectors.toList());
  }

  private String toType(FieldDescriptor field) {
    String type = TYPES.get(field.getType());
    if (type != null) {
      return type;
    }
    return imports.importGenericDescriptor(getGenericDescriptor(field));
  }

  private List<String> getNestedMessages(Descriptor descriptor, int level) {
    Set<String> mapFields =
        descriptor.getFields().stream()
            .filter(FieldDescriptor::isMapField)
            .map(fieldDescriptor -> StringUtils.capitalize(fieldDescriptor.getName()) + "Entry")
            .collect(Collectors.toSet());

    return descriptor.getNestedTypes().stream()
        .filter(e -> !mapFields.contains(e.getName()))
        .map(e -> generate(() -> getMessageData(e, level), "nested.message.ftl", level))
        .collect(Collectors.toList());
  }

  private List<String> getSubValues(Descriptor descriptor) {
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    return opts.getSubValuesList();
  }
}
