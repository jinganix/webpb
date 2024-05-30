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

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumValueOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptEnumValueOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.TsFileOpts;
import io.github.jinganix.webpb.utilities.utils.OptionUtils;
import io.github.jinganix.webpb.utilities.utils.Templates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/** Generator for enum definition. */
public class EnumGenerator {

  private final Templates templates = new Templates();
  private final TsFileOpts webpbOpts;
  private final TsFileOpts fileOpts;
  private boolean stringValue;

  /**
   * Constructor.
   *
   * @param fd {@link FileDescriptor}
   */
  public EnumGenerator(FileDescriptor fd) {
    this.webpbOpts = OptionUtils.getWebpbOpts(fd, WebpbExtend.FileOpts::hasTs).getTs();
    this.fileOpts = OptionUtils.getOpts(fd, WebpbExtend.FileOpts::hasTs).getTs();
  }

  /**
   * Generate enum declaration.
   *
   * @param descriptor {@link EnumDescriptor}
   * @return {@link String}
   */
  public String generate(EnumDescriptor descriptor) {
    this.stringValue = OptionUtils.isStringValue(descriptor);
    Map<String, Object> data = new HashMap<>();
    data.put("className", descriptor.getName());
    data.put("enums", getEnums(descriptor));
    if (isDefaultConstEnum()) {
      return templates.process("const.enum.ftl", data);
    }
    return templates.process("enum.ftl", data);
  }

  private boolean isDefaultConstEnum() {
    if (fileOpts.hasDefaultConstEnum()) {
      return fileOpts.getDefaultConstEnum();
    }
    if (webpbOpts.hasDefaultConstEnum()) {
      return webpbOpts.getDefaultConstEnum();
    }
    return false;
  }

  private List<Map<String, String>> getEnums(EnumDescriptor enumDescriptor) {
    List<Map<String, String>> enums = new ArrayList<>();
    for (EnumValueDescriptor descriptor : enumDescriptor.getValues()) {
      Map<String, String> data = new HashMap<>();
      data.put("name", descriptor.getName());
      data.put("value", getEnumValue(descriptor));
      enums.add(data);
    }
    return enums;
  }

  private String getEnumValue(EnumValueDescriptor descriptor) {
    OptEnumValueOpts opts = OptionUtils.getOpts(descriptor, EnumValueOpts::hasOpt).getOpt();
    if (StringUtils.isEmpty(opts.getValue())) {
      if (this.stringValue) {
        return "\"" + descriptor.getName() + "\"";
      } else {
        return String.valueOf(descriptor.getNumber());
      }
    }
    return "\"" + opts.getValue() + "\"";
  }
}
