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

package io.github.jinganix.webpb.runtime.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EnumerationSerializer")
class EnumerationSerializerTest {

  @Nested
  @DisplayName("serialize")
  class SerializeTest {

    @Nested
    @DisplayName("when serialize the integer enumeration")
    class WhenSerializeTheIntegerEnumerationTest {

      @Test
      @DisplayName("then serialize successfully")
      void thenSerializeSuccessfully() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals("1", objectMapper.writeValueAsString(IntegerEnum.A));
        assertEquals("2", objectMapper.writeValueAsString(IntegerEnum.B));
      }
    }

    @Nested
    @DisplayName("when serialize the string enumeration")
    class WhenSerializeTheStringEnumerationTest {

      @Test
      @DisplayName("then serialize successfully")
      void thenSerializeSuccessfully() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals("\"val_a\"", objectMapper.writeValueAsString(StringEnum.A));
        assertEquals("\"val_b\"", objectMapper.writeValueAsString(StringEnum.B));
      }
    }

    @Nested
    @DisplayName("when serialize the unhandled enumeration")
    class WhenSerializeTheUnhandledEnumerationTest {

      @Test
      @DisplayName("then throws IllegalArgumentException")
      void thenWriteStringIsCalled() {
        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(JsonMappingException.class, () -> objectMapper.writeValueAsString(LongEnum.A));
      }
    }
  }
}
