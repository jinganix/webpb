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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.AccessDeniedException;
import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void shouldCallUncheckedSuccess() {
    assertTrue(Utils.uncheckedCall(() -> true));
  }

  @Test
  void shouldUncheckedCallThrowRuntimeException() {
    assertThrows(
        RuntimeException.class,
        () ->
            Utils.uncheckedCall(
                () -> {
                  throw new RuntimeException();
                }));
  }

  @Test
  void shouldUncheckedCallAlwaysThrowRuntimeException() {
    assertThrows(
        RuntimeException.class,
        () ->
            Utils.uncheckedCall(
                () -> {
                  throw new AccessDeniedException("Denied");
                }));
  }

  @Test
  void shouldOrEmptyReturnValueGivenValueIsNotEmpty() {
    assertEquals("hello", Utils.orEmpty("hello"));
  }

  @Test
  void shouldOrEmptyReturnEmptyStringGivenValueIsNull() {
    assertEquals("", Utils.orEmpty(null));
  }
}
