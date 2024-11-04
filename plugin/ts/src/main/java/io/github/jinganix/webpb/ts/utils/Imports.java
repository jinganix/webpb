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

package io.github.jinganix.webpb.ts.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/** Imports manager. */
public class Imports {

  private static final Pattern PREFIX = Pattern.compile("(?<prefix>^[./]+)");

  private final List<ImportPath> imported = new ArrayList<>();

  private final String packageName;

  private final List<String> imports;

  private final List<String> lookup;

  /** Constructor. */
  public Imports() {
    this.packageName = "";
    this.imports = new ArrayList<>();
    this.lookup = new ArrayList<>();
  }

  /**
   * Constructor.
   *
   * @param packageName package name
   * @param lookup lookup
   */
  public Imports(String packageName, List<String> imports, List<String> lookup) {
    this.packageName = packageName;
    this.imports = imports;
    this.lookup = lookup;
  }

  /**
   * Check and import a path.
   *
   * @param importPath {@link ImportPath}
   */
  public void importPath(ImportPath importPath) {
    if (!StringUtils.equals(importPath.getName(), this.packageName)) {
      addImported(importPath);
    }
  }

  /**
   * Check and import a type.
   *
   * @param type to import.
   */
  public String importType(String type) {
    String[] parts = StringUtils.split(type, ".");
    if (parts.length == 1) {
      String typeTail = "/" + parts[0];
      for (String s : lookup) {
        if (s.endsWith(typeTail)) {
          String prefix = "";
          String relative = s;
          Matcher matcher = PREFIX.matcher(s);
          if (matcher.find()) {
            prefix = matcher.group("prefix");
            relative = StringUtils.removeStart(s, prefix);
          }
          String path = StringUtils.removeEnd(relative, "/" + type);
          String name = path.substring(path.lastIndexOf("/") + 1);
          int order = prefix.isEmpty() || prefix.startsWith("/") ? -1 : 0;
          addImported(new ImportPath(name, prefix + path, order));
          return name + "." + type;
        }
      }
      return type;
    }
    if (StringUtils.equals(parts[0], this.packageName)) {
      return type.substring(type.indexOf(".") + 1);
    }
    addImported(new ImportPath(parts[0], "./" + parts[0]));
    return type;
  }

  private void addImported(ImportPath importPath) {
    for (ImportPath path : imported) {
      if (path.getName().equals(importPath.getName())) {
        return;
      }
    }
    imported.add(importPath);
  }

  /**
   * Return imported string list.
   *
   * @return imported data
   */
  public List<String> toList() {
    List<String> imports =
        imported.stream()
            .sorted(
                (o1, o2) -> {
                  if (o1.getOrder() != 0 || o2.getOrder() != 0) {
                    int res = Integer.compare(o1.getOrder(), o2.getOrder());
                    if (res != 0) {
                      return res;
                    }
                  }
                  return StringUtils.compare(o1.getName(), o2.getName());
                })
            .map(e -> "import * as " + e.getName() + " from \"" + e.getPath() + "\";")
            .collect(Collectors.toList());
    imports.addAll(this.imports);
    return imports;
  }
}
