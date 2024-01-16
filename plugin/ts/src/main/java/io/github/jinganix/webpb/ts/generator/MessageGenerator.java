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

package io.github.jinganix.webpb.ts.generator;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.LONG;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.STRING;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypeFullName;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapKeyDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapValueDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.isMessage;
import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.commons.SegmentGroup;
import io.github.jinganix.webpb.commons.UrlSegment;
import io.github.jinganix.webpb.ts.utils.ImportPath;
import io.github.jinganix.webpb.ts.utils.Imports;
import io.github.jinganix.webpb.ts.utils.TsUtils;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FieldOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptFieldOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.TsFileOpts;
import io.github.jinganix.webpb.utilities.utils.AliasUtils;
import io.github.jinganix.webpb.utilities.utils.DescriptorUtils;
import io.github.jinganix.webpb.utilities.utils.OptionUtils;
import io.github.jinganix.webpb.utilities.utils.Templates;
import io.github.jinganix.webpb.utilities.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/** Generator for {@link Descriptor}. */
public class MessageGenerator {

  private static final String UNKNOWN = "unknown";

  private static final Map<FieldDescriptor.Type, String> TYPES;

  static {
    Map<FieldDescriptor.Type, String> map = new HashMap<>();
    map.put(FieldDescriptor.Type.BOOL, "boolean");
    map.put(FieldDescriptor.Type.BYTES, "Uint8Array");
    map.put(FieldDescriptor.Type.DOUBLE, "number");
    map.put(FieldDescriptor.Type.FLOAT, "number");
    map.put(FieldDescriptor.Type.FIXED32, "number");
    map.put(FieldDescriptor.Type.FIXED64, "number");
    map.put(FieldDescriptor.Type.INT32, "number");
    map.put(FieldDescriptor.Type.INT64, "number");
    map.put(FieldDescriptor.Type.SFIXED32, "number");
    map.put(FieldDescriptor.Type.SFIXED64, "number");
    map.put(FieldDescriptor.Type.SINT32, "number");
    map.put(FieldDescriptor.Type.SINT64, "number");
    map.put(FieldDescriptor.Type.STRING, "string");
    map.put(FieldDescriptor.Type.UINT32, "number");
    map.put(FieldDescriptor.Type.UINT64, "number");
    TYPES = map;
  }

  private final Templates templates = new Templates();

  private final Imports imports;

  private final TsFileOpts webpbOpts;

  private final TsFileOpts fileOpts;

  /**
   * Constructor.
   *
   * @param fd {@link FileDescriptor}
   */
  public MessageGenerator(Imports imports, FileDescriptor fd) {
    this.imports = imports;
    this.webpbOpts = OptionUtils.getWebpbOpts(fd, WebpbExtend.FileOpts::hasTs).getTs();
    this.fileOpts = OptionUtils.getOpts(fd, WebpbExtend.FileOpts::hasTs).getTs();
  }

  /**
   * Entrance of the generator.
   *
   * @param descriptor {@link Descriptor}
   * @return generated file content
   */
  public String generate(Descriptor descriptor) {
    return generate(() -> getMessageData(descriptor, 0), "message.ftl", 0);
  }

  private String generate(Supplier<Map<String, Object>> supplier, String ftl, int level) {
    imports.importPath(new ImportPath("Webpb", "webpb", Integer.MIN_VALUE));
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
    OptionUtils.checkDuplicatedFields(descriptor);
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    Map<String, Object> data = new HashMap<>();
    data.put("extendI", TsUtils.toInterfaceName(getExtend(descriptor)));
    data.put("extend", getExtend(descriptor));
    data.put("className", descriptor.getName());
    data.put("method", opts.getMethod());
    data.put("context", Utils.normalize(opts.getContext()));
    data.put("path", getPath(descriptor, Utils.normalize(opts.getPath())));
    data.put("fields", getFields(descriptor));
    data.put("nestedMsgs", getNestedMessages(descriptor, level + 1));
    data.put("omitted", getOmitted(descriptor));
    data.put("hasAlias", hasAlias(descriptor));
    data.put("aliases", getAliases(descriptor));
    data.put("aliasMsgs", getAliasMsgs(descriptor));
    return data;
  }

  private List<String> getOmitted(Descriptor descriptor) {
    List<String> omitted = new ArrayList<>();
    for (FieldDescriptor fieldDescriptor : descriptor.getFields()) {
      OptFieldOpts fieldOpts = getOpts(fieldDescriptor, FieldOpts::hasOpt).getOpt();
      if (fieldOpts.getInQuery() || fieldOpts.getOmitted()) {
        omitted.add(fieldDescriptor.getName());
      }
    }
    return omitted;
  }

  private boolean hasAlias(Descriptor descriptor) {
    for (FieldDescriptor fieldDescriptor : descriptor.getFields()) {
      if (isNotEmpty(getOpts(fieldDescriptor, FieldOpts::hasTs).getTs().getAlias())) {
        return true;
      }
      if (isAutoAlias(descriptor, fieldDescriptor)) {
        return true;
      }
      if (!isMessage(fieldDescriptor)) {
        continue;
      }
      if (hasAlias(fieldDescriptor.getMessageType())) {
        return true;
      }
    }
    return false;
  }

  private boolean isAutoAlias(Descriptor descriptor, FieldDescriptor fieldDescriptor) {
    return webpbOpts.getAutoAlias()
        || fileOpts.getAutoAlias()
        || getOpts(descriptor, MessageOpts::hasTs).getTs().getAutoAlias()
        || getOpts(fieldDescriptor, FieldOpts::hasTs).getTs().getAutoAlias();
  }

  private Map<String, String> getAliases(Descriptor descriptor) {
    Map<String, String> autoAliases = AliasUtils.getAutoAliases(descriptor);
    List<FieldDescriptor> fields = OptionUtils.getAllFields(descriptor);
    Map<String, String> aliases = new TreeMap<>();
    for (FieldDescriptor fieldDescriptor : fields) {
      String alias = getOpts(fieldDescriptor, FieldOpts::hasTs).getTs().getAlias();
      if (StringUtils.isNotEmpty(alias)) {
        aliases.put(fieldDescriptor.getName(), alias);
      } else if (isAutoAlias(descriptor, fieldDescriptor)) {
        aliases.put(fieldDescriptor.getName(), autoAliases.get(fieldDescriptor.getName()));
      }
    }
    return aliases;
  }

  private String getExtend(Descriptor descriptor) {
    OptMessageOpts opts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
    if (StringUtils.isNotEmpty(opts.getExtends())) {
      return imports.importType(opts.getExtends());
    }
    return null;
  }

  private List<Map<String, String>> getAliasMsgs(Descriptor descriptor) {
    List<Map<String, String>> aliasMsgs = new ArrayList<>();
    for (FieldDescriptor field : descriptor.getFields()) {
      FieldDescriptor value = field.isMapField() ? getMapValueDescriptor(field) : field;
      if (!isMessage(value)) {
        continue;
      }
      String type = toType(value, false);
      if (UNKNOWN.equals(type)) {
        continue;
      }
      Map<String, String> data = new HashMap<>();
      data.put("name", field.getName());
      data.put("type", type);
      data.put("collection", field.isMapField() ? "map" : field.isRepeated() ? "list" : "none");
      aliasMsgs.add(data);
    }
    aliasMsgs.sort(Comparator.comparing(o -> o.getOrDefault("name", "")));
    return aliasMsgs;
  }

  private Map<String, Object> getPath(Descriptor descriptor, String path) {
    if (StringUtils.isEmpty(path)) {
      return Collections.emptyMap();
    }
    SegmentGroup group = SegmentGroup.of(path);
    DescriptorUtils.validation(group, descriptor);
    Map<String, Object> data = new HashMap<>();
    data.put("raw", path);
    data.put("url", formatUrl(group));
    data.put("queries", getQueries(group));
    return data;
  }

  private String formatUrl(SegmentGroup group) {
    StringBuilder builder = new StringBuilder();
    for (UrlSegment segment : group.getPathSegments()) {
      builder.append(segment.getPrefix());
      builder.append("${").append(getter(segment.getValue())).append("}");
    }
    builder.append(group.getSuffix());
    return builder.toString();
  }

  private List<Map<String, String>> getQueries(SegmentGroup group) {
    if (group.getQuerySegments().isEmpty()) {
      return Collections.emptyList();
    }
    List<Map<String, String>> data = new ArrayList<>();
    for (UrlSegment seg : group.getQuerySegments()) {
      Map<String, String> pair = new HashMap<>();
      pair.put("key", seg.getKey());
      pair.put("value", seg.isAccessor() ? getter(seg.getValue()) : "\"" + seg.getValue() + "\"");
      data.add(pair);
    }
    return data;
  }

  private String getter(String value) {
    StringBuilder builder = new StringBuilder();
    if (value.contains(".")) {
      builder.append("Webpb.getter(p, \"").append(value).append("\")");
    } else {
      builder.append("p?.").append(value);
    }
    return builder.toString();
  }

  private List<Map<String, Object>> getFields(Descriptor descriptor) {
    List<Map<String, Object>> fields = new ArrayList<>();
    for (FieldDescriptor field : descriptor.getFields()) {
      Map<String, Object> data = new HashMap<>();
      data.put("type", getFieldType(field));
      data.put("name", field.getName());
      data.put("optional", field.isOptional());
      if (containsMessage(field)) {
        String msgType = toType(field.isMapField() ? getMapValueDescriptor(field) : field, false);
        if (!UNKNOWN.equals(msgType)) {
          data.put("msgType", msgType);
        }
      }
      data.put("collection", field.isMapField() ? "map" : field.isRepeated() ? "list" : "none");
      if (field.hasDefaultValue()) {
        Object value = field.getDefaultValue();
        data.put("default", field.getJavaType() == STRING ? "\"" + value + "\"" : value);
      }
      fields.add(data);
    }
    return fields;
  }

  private String getFieldType(FieldDescriptor field) {
    if (field.isMapField()) {
      FieldDescriptor key = getMapKeyDescriptor(field);
      FieldDescriptor value = getMapValueDescriptor(field);
      return "Record<" + toType(key, false) + ", " + toType(value, true) + ">";
    } else if (field.isRepeated()) {
      return toType(field, true) + "[]";
    }
    return toType(field, true);
  }

  private boolean containsMessage(FieldDescriptor field) {
    if (!isMessage(field)) {
      return false;
    }
    field = field.isMapField() ? getMapValueDescriptor(field) : field;
    FieldDescriptor.Type type = field.getType();
    if (TYPES.containsKey(type)) {
      return false;
    }
    String fullName = getFieldTypeFullName(field);
    return !"google.protobuf.Any".equals(fullName);
  }

  private String toType(FieldDescriptor field, boolean toI) {
    if (field.getJavaType() == LONG) {
      if (webpbOpts.getInt64AsString()) {
        return "string";
      } else if (fileOpts.getInt64AsString()) {
        return "string";
      } else if (getOpts(field, FieldOpts::hasTs).getTs().getAsString()) {
        return "string";
      }
    }
    FieldDescriptor.Type type = field.getType();
    if (TYPES.containsKey(type)) {
      return TYPES.get(type);
    }

    String fullName = getFieldTypeFullName(field);
    if ("google.protobuf.Any".equals(fullName)) {
      return UNKNOWN;
    }
    String typeName = (toI && isMessage(field)) ? TsUtils.toInterfaceName(fullName) : fullName;
    return imports.importType(typeName);
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
}
