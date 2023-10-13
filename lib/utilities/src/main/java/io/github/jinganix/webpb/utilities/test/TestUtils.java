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

package io.github.jinganix.webpb.utilities.test;

import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

/** Utilities for test. */
public class TestUtils {

  private TestUtils() {}

  /**
   * Create a {@link RequestContext} from {@link Dump}.
   *
   * @param dump {@link Dump}
   * @return {@link RequestContext}
   */
  public static RequestContext createRequest(Dump dump) {
    dump.pipe();
    try {
      return new RequestContext();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Read an expected file content from resources dir.
   *
   * @param filename filename to read
   * @return file content
   */
  public static String readFile(String filename) {
    try {
      InputStream inputStream = TestUtils.class.getResourceAsStream(filename);
      if (inputStream == null) {
        throw new NullPointerException("file not exists: " + filename);
      }
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Compare text to a file content.
   *
   * @param text text to compare
   * @param filename file to compare
   * @param exactly ignore spaces/tabs/newlines when not exactly
   * @return true if same
   */
  public static boolean compareToFile(String text, String filename, boolean exactly) {
    try {
      InputStream inputStream = TestUtils.class.getResourceAsStream(filename);
      if (inputStream == null) {
        throw new NullPointerException("file not exists: " + filename);
      }
      String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      if (exactly) {
        return text.equals(content);
      }
      return text.replaceAll("\\s+", "").equals(content.replaceAll("\\s+", ""));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
