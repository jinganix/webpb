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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("EnumerationDeserializer")
class EnumerationKeyDeserializerTest {

  @Nested
  @DisplayName("deserialize")
  class DeserializeTest {

    @Nested
    @DisplayName("when deserialize from map data")
    class WhenSerializeTheIntegerEnumerationTest {

      @DisplayName("then deserialize the enum key")
      @Test
      void thenDeserializeTheEnumKey() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<IntegerEnum, Integer> data =
            objectMapper.readValue(
                "{\"1\":1,\"2\":2}", new TypeReference<Map<IntegerEnum, Integer>>() {});
        assertEquals(2, data.size());
        assertEquals(1, data.get(IntegerEnum.A));
        assertEquals(2, data.get(IntegerEnum.B));
      }
    }

    @Nested
    @DisplayName("when deserialize from map data")
    class WhenSerializeTheStringEnumerationTest {

      @Test
      @DisplayName("then deserialize the enum key")
      void thenDeserializeTheEnumKey() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<StringEnum, Integer> data =
            objectMapper.readValue(
                "{\"val_a\":1,\"val_b\":2}", new TypeReference<Map<StringEnum, Integer>>() {});
        assertEquals(2, data.size());
        assertEquals(1, data.get(StringEnum.A));
        assertEquals(2, data.get(StringEnum.B));
      }
    }
  }
}
