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

package io.github.jinganix.webpb.runtime.model;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/** {@link FooResponse}. */
@Accessors(chain = true)
@Getter
@Setter
public class FooResponse implements WebpbMessage {

  private Long id;

  /**
   * Get {@link WebpbMeta}.
   *
   * @return {@link WebpbMeta}
   */
  @Override
  public WebpbMeta webpbMeta() {
    return null;
  }
}
