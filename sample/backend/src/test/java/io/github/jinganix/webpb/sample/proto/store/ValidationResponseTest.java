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

package io.github.jinganix.webpb.sample.proto.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ValidationResponse")
class ValidationResponseTest {

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Nested
    @DisplayName("when construct without errors")
    class WhenConstructWithoutErrors {

      @Test
      @DisplayName("then errors is null")
      void thenErrorsIsNull() {
        ValidationResponse response = new ValidationResponse();
        assertThat(response.webpbMeta()).isNotNull();
        assertThat(response.getErrors()).isNull();
        ;
      }
    }

    @Nested
    @DisplayName("when construct with errors")
    class WhenConstructWithErrors {

      @Test
      @DisplayName("then contains the errors")
      void thenContainsTheErrors() {
        Map<String, String> errors = Collections.emptyMap();
        ValidationResponse response = new ValidationResponse(errors);
        assertThat(response.webpbMeta()).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
      }
    }
  }

  @Nested
  @DisplayName("setErrors")
  class SetErrors {

    @Nested
    @DisplayName("when called")
    class WhenCalled {

      @Test
      @DisplayName("then contains the errors")
      void thenContainsTheErrors() {
        Map<String, String> errors = Collections.emptyMap();
        ValidationResponse response = new ValidationResponse().setErrors(errors);
        assertThat(response.webpbMeta()).isNotNull();
        assertThat(response.getErrors()).isEqualTo(errors);
        ;
      }
    }
  }
}
