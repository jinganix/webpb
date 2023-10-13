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

package io.github.jinganix.webpb.sample.proto.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PageablePbTest {

  @Test
  void test() {
    PageablePb pb = new PageablePb().setPagination(true).setPage(11).setSize(22).setSort("SORT");
    assertNotNull(pb.webpbMeta());
    assertEquals(true, pb.getPagination());
    assertEquals(11, pb.getPage());
    assertEquals(22, pb.getSize());
    assertEquals("SORT", pb.getSort());
  }
}
