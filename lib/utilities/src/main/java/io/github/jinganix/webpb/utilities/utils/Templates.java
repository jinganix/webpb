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

package io.github.jinganix.webpb.utilities.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.util.Map;

/** Tool to handle freemarker templates. */
public class Templates {

  private final Configuration configuration;

  /** Constructor. */
  public Templates() {
    this.configuration = new Configuration(Configuration.VERSION_2_3_32);
    this.configuration.setClassForTemplateLoading(this.getClass(), "/templates");
  }

  /**
   * Process a template with data input.
   *
   * @param ftl template file
   * @param data input data
   * @return processed content
   */
  public String process(String ftl, Map<String, Object> data) {
    try {
      Template template = this.configuration.getTemplate(ftl);
      StringWriter writer = new StringWriter();
      template.process(data, writer);
      return writer.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
