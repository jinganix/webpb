/*
 * Copyright (c) 2020 jinganix@gmail.com, All Rights Reserved.
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
 */

package io.github.jinganix.webpb.runtime.mvc;

import static io.github.jinganix.webpb.runtime.mvc.WebpbRequestUtils.mergeVariables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WebpbRequestUtilsTest {

  @Test
  void givenTwoEmptyMaps_ThenMergeSuccess() {
    assertTrue(mergeVariables(Collections.emptyMap(), Collections.emptyMap()).isEmpty());
  }

  @Test
  void givenAttributesNotEmpty_ThenMergeSuccess() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("a", "b");
    Map<String, String> map = mergeVariables(attributes, Collections.emptyMap());
    assertEquals(1, map.size());
    assertTrue(map.containsKey("a"));
  }

  @Test
  void givenParameterMapNotEmpty_ThenMergeSuccess() {
    Map<String, String[]> parameters = new HashMap<>();
    parameters.put("a", new String[] {"b"});
    Map<String, String> map = mergeVariables(Collections.emptyMap(), parameters);
    assertEquals(1, map.size());
    assertTrue(map.containsKey("a"));
  }

  @Test
  void givenTwoNonEmptyMaps_ThenMergeSuccess() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("a", "b");

    Map<String, String[]> parameters = new HashMap<>();
    parameters.put("c", new String[] {"d"});

    Map<String, String> map = mergeVariables(attributes, parameters);
    assertEquals(2, map.size());
    assertTrue(map.containsKey("a"));
    assertTrue(map.containsKey("c"));
  }
}
