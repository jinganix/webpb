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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Const")
class ConstTest {

  @Test
  @DisplayName("should match webpb options filenames when pattern applies")
  void shouldMatchWebpbOptionsFilenamesWhenPatternApplies() {
    // When / Then
    assertThat("WebpbOptions.".matches(Const.WEBPB_OPTIONS)).isFalse();
    assertThat("WebpbOptions.prot".matches(Const.WEBPB_OPTIONS)).isFalse();
    assertThat("aWebpbOptions.prot".matches(Const.WEBPB_OPTIONS)).isFalse();
    assertThat("WEBPBOptions.proTo".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("WebpbOptions.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("a/WebpbOptions.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("a/b/WebpbOptions.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("weBpb_optIons.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("webpb-options.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("webpb.options.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
    assertThat("webpb_options.proto".matches(Const.WEBPB_OPTIONS)).isTrue();
  }
}
