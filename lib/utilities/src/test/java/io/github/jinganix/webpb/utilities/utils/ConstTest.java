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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConstTest {

  @Test
  void shouldMatchWebpbOptionsFileSuccess() {
    assertFalse("WebpbOptions.".matches(Const.WEBPB_OPTIONS));
    assertFalse("WebpbOptions.prot".matches(Const.WEBPB_OPTIONS));
    assertFalse("aWebpbOptions.prot".matches(Const.WEBPB_OPTIONS));
    assertTrue("WEBPBOptions.proTo".matches(Const.WEBPB_OPTIONS));
    assertTrue("WebpbOptions.proto".matches(Const.WEBPB_OPTIONS));
    assertTrue("a/WebpbOptions.proto".matches(Const.WEBPB_OPTIONS));
    assertTrue("a/b/WebpbOptions.proto".matches(Const.WEBPB_OPTIONS));
    assertTrue("weBpb_optIons.proto".matches(Const.WEBPB_OPTIONS));
    assertTrue("webpb-options.proto".matches(Const.WEBPB_OPTIONS));
    assertTrue("webpb.options.proto".matches(Const.WEBPB_OPTIONS));
    assertTrue("webpb_options.proto".matches(Const.WEBPB_OPTIONS));
  }
}
