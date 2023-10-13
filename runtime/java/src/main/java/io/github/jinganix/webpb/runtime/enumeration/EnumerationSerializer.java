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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/** EnumerationSerializer. */
public class EnumerationSerializer extends StdSerializer<Enumeration<?>> {

  /** Constructor. */
  public EnumerationSerializer() {
    this(null);
  }

  /**
   * Constructor.
   *
   * @param t class of the {@link Enumeration} object
   */
  public EnumerationSerializer(Class<Enumeration<?>> t) {
    super(t);
  }

  /**
   * Serialize an {@link Enumeration} value.
   *
   * @param value Value to serialize; can <b>not</b> be null.
   * @param gen Generator used to output resulting Json content
   * @param provider Provider that can be used to get serializers for serializing Objects value
   *     contains, if any.
   * @throws IOException throw exception when failed
   */
  @Override
  public void serialize(Enumeration value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    if (value.getValue() instanceof Integer) {
      gen.writeNumber((Integer) value.getValue());
    } else if (value.getValue() instanceof String) {
      gen.writeString((String) value.getValue());
    } else {
      throw new IllegalArgumentException();
    }
  }
}
