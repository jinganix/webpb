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

import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveEnum;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveFile;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumValueOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FieldOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FileOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OptionUtilsTest {

  // FileOpts
  @Test
  void shouldGetFileOptsSuccess() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "Test.proto");

    FileOpts javaOpts = OptionUtils.getOpts(descriptor, FileOpts::hasJava);
    assertTrue(javaOpts.getJava().getGenGetter());

    FileOpts tsOpts = OptionUtils.getOpts(descriptor, FileOpts::hasTs);
    assertTrue(tsOpts.getTs().getInt64AsString());
  }

  @Test
  void shouldGetFileOptsSuccessWhenParseError() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "Test.proto");
    try (MockedStatic<FileOpts> opts = mockStatic(FileOpts.class)) {
      opts.when(() -> FileOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertEquals(FileOpts.getDefaultInstance(), OptionUtils.getOpts(descriptor, o -> true));
    }
  }

  @Test
  void shouldGetFileOptsSuccessWhenWithoutOptions() {
    RequestContext context = TestUtils.createRequest(Dump.test2);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "Test1.proto");
    assertEquals(
        FileOpts.getDefaultInstance(), OptionUtils.getOpts((FileDescriptor) null, o -> true));
    assertEquals(FileOpts.getDefaultInstance(), OptionUtils.getOpts(descriptor, o -> true));
  }

  // MessageOpts
  @Test
  void shouldGetMessageOptsSuccess() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");

    MessageOpts optOpts = OptionUtils.getOpts(descriptor, MessageOpts::hasOpt);
    assertEquals("GET", optOpts.getOpt().getMethod());

    MessageOpts javaOpts = OptionUtils.getOpts(descriptor, MessageOpts::hasJava);
    assertEquals(2, javaOpts.getJava().getAnnotationCount());
  }

  @Test
  void shouldGetMessageOptsSuccessWhenParseError() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    try (MockedStatic<MessageOpts> opts = mockStatic(MessageOpts.class)) {
      opts.when(() -> MessageOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertEquals(MessageOpts.getDefaultInstance(), OptionUtils.getOpts(descriptor, o -> true));
    }
  }

  @Test
  void shouldGetMessageOptsSuccessWhenWithoutOptions() {
    RequestContext context = TestUtils.createRequest(Dump.test2);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test1");
    assertEquals(
        MessageOpts.getDefaultInstance(), OptionUtils.getOpts((Descriptor) null, o -> true));
    assertEquals(MessageOpts.getDefaultInstance(), OptionUtils.getOpts(descriptor, o -> true));
  }

  // EnumOpts
  @Test
  void shouldGetEnumOptsSuccess() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    EnumDescriptor enumDescriptor = resolveEnum(context.getDescriptors(), "Enum");

    EnumOpts optOpts = OptionUtils.getOpts(enumDescriptor, EnumOpts::hasOpt);
    assertNotNull(optOpts);

    EnumOpts javaOpts = OptionUtils.getOpts(enumDescriptor, EnumOpts::hasJava);
    assertEquals(2, javaOpts.getJava().getAnnotationCount());
  }

  @Test
  void shouldGetEnumOptsSuccessWhenParseError() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    EnumDescriptor enumDescriptor = resolveEnum(context.getDescriptors(), "Enum");
    try (MockedStatic<EnumOpts> opts = mockStatic(EnumOpts.class)) {
      opts.when(() -> EnumOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertEquals(EnumOpts.getDefaultInstance(), OptionUtils.getOpts(enumDescriptor, o -> true));
    }
  }

  @Test
  void shouldGetEnumOptsSuccessWhenWithoutOptions() {
    RequestContext context = TestUtils.createRequest(Dump.test2);
    EnumDescriptor enumDescriptor = resolveEnum(context.getDescriptors(), "Enum");
    assertEquals(
        EnumOpts.getDefaultInstance(), OptionUtils.getOpts((EnumDescriptor) null, o -> true));
    assertEquals(EnumOpts.getDefaultInstance(), OptionUtils.getOpts(enumDescriptor, o -> true));
  }

  // FieldOpts
  @Test
  void shouldGetFieldOptsSuccess() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);

    FieldOpts optOpts = OptionUtils.getOpts(fieldDescriptor, FieldOpts::hasOpt);
    assertTrue(optOpts.getOpt().getInQuery());

    FieldOpts tsOpts = OptionUtils.getOpts(fieldDescriptor, FieldOpts::hasTs);
    assertTrue(tsOpts.getTs().getAsString());
  }

  @Test
  void shouldGetFieldOptsSuccessWhenParseError() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);
    try (MockedStatic<FieldOpts> opts = mockStatic(FieldOpts.class)) {
      opts.when(() -> FieldOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertEquals(FieldOpts.getDefaultInstance(), OptionUtils.getOpts(fieldDescriptor, o -> true));
    }
  }

  @Test
  void shouldGetFieldOptsSuccessWhenWithoutOptions() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test6");
    assertNotNull(descriptor);
    FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);
    assertEquals(
        FieldOpts.getDefaultInstance(), OptionUtils.getOpts((FieldDescriptor) null, o -> true));
    assertEquals(FieldOpts.getDefaultInstance(), OptionUtils.getOpts(fieldDescriptor, o -> true));
  }

  // EnumValueOpts
  @Test
  void shouldGetEnumValueOptsSuccess() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    EnumDescriptor descriptor = resolveEnum(context.getDescriptors(), "Test5");
    assertNotNull(descriptor);
    EnumValueDescriptor enumValueDescriptor = descriptor.getValues().get(0);

    EnumValueOpts javaOpts = OptionUtils.getOpts(enumValueDescriptor, EnumValueOpts::hasJava);
    assertEquals(0, javaOpts.getJava().getAnnotationCount());

    EnumValueOpts optOpts = OptionUtils.getOpts(enumValueDescriptor, EnumValueOpts::hasOpt);
    assertEquals("text1", optOpts.getOpt().getValue());
  }

  @Test
  void shouldGetEnumValueOptsSuccessWhenParseError() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    EnumDescriptor descriptor = resolveEnum(context.getDescriptors(), "Test5");
    assertNotNull(descriptor);
    EnumValueDescriptor enumValueDescriptor = descriptor.getValues().get(0);
    try (MockedStatic<EnumValueOpts> opts = mockStatic(EnumValueOpts.class)) {
      opts.when(() -> EnumValueOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertEquals(
          EnumValueOpts.getDefaultInstance(), OptionUtils.getOpts(enumValueDescriptor, o -> true));
    }
  }

  @Test
  void shouldGetEnumValueOptsSuccessWhenWithoutOptions() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    EnumDescriptor descriptor = resolveEnum(context.getDescriptors(), "Test5");
    assertNotNull(descriptor);
    EnumValueDescriptor enumValueDescriptor = descriptor.getValues().get(2);
    assertEquals(
        EnumValueOpts.getDefaultInstance(),
        OptionUtils.getOpts((EnumValueDescriptor) null, o -> true));
    assertEquals(
        EnumValueOpts.getDefaultInstance(), OptionUtils.getOpts(enumValueDescriptor, o -> true));
  }

  @Test
  void shouldReturnEnumIsStringValueSuccess() {
    RequestContext context = TestUtils.createRequest(Dump.test1);
    EnumDescriptor descriptor1 = resolveEnum(context.getDescriptors(), "Test3");
    assertTrue(OptionUtils.isStringValue(descriptor1));

    EnumDescriptor descriptor2 = resolveEnum(context.getDescriptors(), "Test5");
    assertTrue(OptionUtils.isStringValue(descriptor2));

    EnumDescriptor descriptor3 = resolveEnum(context.getDescriptors(), "Enum");
    assertFalse(OptionUtils.isStringValue(descriptor3));
  }

  @Nested
  @DisplayName("checkDuplicatedFields")
  class CheckDuplicatedFields {

    @Nested
    @DisplayName("when there are duplicated fields")
    class WhenThereAreDuplicatedFields {

      @Test
      @DisplayName("then throw exception")
      void thenThrowException() {
        RequestContext context = TestUtils.createRequest(Dump.error_test);
        Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test2");
        assertThatThrownBy(() -> OptionUtils.checkDuplicatedFields(descriptor))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Duplicated field name `Test2.foo` in DuplicatedFieldsError.proto when extends");
      }
    }

    @Nested
    @DisplayName("when no duplicated fields")
    class WhenNoDuplicatedFields {

      @Test
      @DisplayName("then not throw exception")
      void thenNotThrowException() {
        RequestContext context = TestUtils.createRequest(Dump.test1);
        Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
        assertThatCode(() -> OptionUtils.checkDuplicatedFields(descriptor))
            .doesNotThrowAnyException();
      }
    }
  }
}
