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

package io.github.jinganix.webpb.runtime.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import io.github.jinganix.webpb.runtime.common.InQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Test class implements {@link WebpbMessage}. */
@Accessors(chain = true)
@Getter
@Setter
public class FooRequest implements WebpbMessage {

  public static final WebpbMeta WEBPB_META = WebpbMeta.builder().build();

  @JsonIgnore
  private WebpbMeta webpbMeta =
      WebpbMeta.builder()
          .method("POST")
          .path("/domain/{id}/action?p={pagination}&size={pageable.size}&page={pageable.page}")
          .build();

  @InQuery private int id = 123;
  @InQuery private boolean pagination = true;
  @InQuery private Pageable pageable = new Pageable().setPage(10).setSize(20);
  @JsonIgnore private String ignored = "IGNORED";
  private String data = "data123";

  public FooRequest() {}

  public FooRequest(WebpbMeta webpbMeta) {
    this.webpbMeta = webpbMeta;
  }

  @Override
  public WebpbMeta webpbMeta() {
    return webpbMeta;
  }
}
