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

package io.github.jinganix.webpb.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SegmentGroup")
class SegmentGroupTest {

  @Nested
  @DisplayName("when path is null")
  class WhenPathIsNull {

    @Test
    @DisplayName("then group is empty")
    void thenGroupIsEmpty() {
      SegmentGroup group = SegmentGroup.of(null);
      assertThat(group.isEmpty()).isTrue();
      assertThat(group.getSuffix()).isEmpty();
    }
  }

  @Nested
  @DisplayName("when path is empty")
  class WhenPathIsEmpty {

    @Test
    @DisplayName("then group is empty")
    void thenGroupIsEmpty() {
      SegmentGroup group = SegmentGroup.of("");
      assertThat(group.isEmpty()).isTrue();
      assertThat(group.getSuffix()).isEmpty();
    }
  }

  @Nested
  @DisplayName("when path is /")
  class WhenPathIsSlash {

    @Test
    @DisplayName("then group is empty")
    void thenGroupIsEmpty() {
      SegmentGroup group = SegmentGroup.of("/");
      assertThat(group.isEmpty()).isTrue();
      assertThat(group.getSuffix()).isEqualTo("/");
    }
  }

  @Nested
  @DisplayName("when url contains path")
  class WhenUrlContainsPath {

    @Test
    @DisplayName("then there are path segments")
    void thenThereArePathSegments() {
      SegmentGroup group = SegmentGroup.of("/{a}/b{c}/{d}e/f{g.h}i/j");

      assertThat(group.getPathSegments()).hasSize(4);
      assertThat(group.getQuerySegments()).isEmpty();
      assertThat(group.getSuffix()).isEqualTo("i/j");

      assertThat(group.getPathSegments().get(0))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, "/", null, "a");

      assertThat(group.getPathSegments().get(1))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, "/b", null, "c");

      assertThat(group.getPathSegments().get(2))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, "/", null, "d");

      assertThat(group.getPathSegments().get(3))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, "e/f", null, "g.h");
    }
  }

  @Nested
  @DisplayName("when url contains query")
  class WhenUrlContainsQuery {

    @Test
    @DisplayName("then there are query segments")
    void thenThereAreQuerySegments() {
      SegmentGroup group = SegmentGroup.of("a={b}&c={d.e}");

      assertThat(group.getPathSegments()).isEmpty();
      assertThat(group.getQuerySegments()).hasSize(2);
      assertThat(group.getSuffix()).isEqualTo("");

      assertThat(group.getQuerySegments().get(0))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, null, "a", "b");

      assertThat(group.getQuerySegments().get(1))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, null, "c", "d.e");
    }
  }

  @Nested
  @DisplayName("when url contains path and query")
  class WhenUrlContainsPathAndQuery {

    @Test
    @DisplayName("then there are segments")
    void thenThereAreSegments() {
      SegmentGroup group = SegmentGroup.of("a/{b}/c?e={f}&g=h&i={j}");

      assertThat(group.getPathSegments()).hasSize(1);
      assertThat(group.getQuerySegments()).hasSize(3);
      assertThat(group.getSuffix()).isEqualTo("/c");

      assertThat(group.getPathSegments().get(0))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, "a/", null, "b");

      assertThat(group.getQuerySegments().get(0))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, null, "e", "f");

      assertThat(group.getQuerySegments().get(1))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(false, null, "g", "h");

      assertThat(group.getQuerySegments().get(2))
          .extracting("accessor", "prefix", "key", "value")
          .containsExactly(true, null, "i", "j");
    }
  }
}
