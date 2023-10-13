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
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EnumerationDeserializer")
class EnumerationDeserializerTest {

  @Nested
  @DisplayName("deserialize")
  class DeserializeTest {

    @Nested
    @DisplayName("when deserialize from integer")
    class WhenSerializeTheIntegerEnumerationTest {

      @Test
      @DisplayName("then deserialize the enum")
      void thenDeserializeTheEnum() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(IntegerEnum.A, objectMapper.readValue("1", IntegerEnum.class));
        assertEquals(IntegerEnum.B, objectMapper.readValue("2", IntegerEnum.class));
        assertNull(objectMapper.readValue("3", IntegerEnum.class));
      }
    }

    @Nested
    @DisplayName("when deserialize from string")
    class WhenSerializeTheStringEnumerationTest {

      @Test
      @DisplayName("then deserialize the enum")
      void thenDeserializeTheEnum() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(StringEnum.A, objectMapper.readValue("\"val_a\"", StringEnum.class));
        assertEquals(StringEnum.B, objectMapper.readValue("\"val_b\"", StringEnum.class));
        assertNull(objectMapper.readValue("\"val_c\"", StringEnum.class));
      }
    }
  }
}
