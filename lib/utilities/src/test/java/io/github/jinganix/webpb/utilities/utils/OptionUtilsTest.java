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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("OptionUtils")
class OptionUtilsTest {

  @Test
  @DisplayName("should return file opts when descriptor has options")
  void shouldReturnFileOptsWhenDescriptorHasOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "CoreMessages.proto");

    // When
    FileOpts javaOpts = OptionUtils.getOpts(descriptor, FileOpts::hasJava);
    FileOpts tsOpts = OptionUtils.getOpts(descriptor, FileOpts::hasTs);

    // Then
    assertThat(javaOpts.getJava().getGenGetter()).isTrue();
    assertThat(tsOpts.getTs().getInt64AsString()).isTrue();
  }

  @Test
  @DisplayName("should return default file opts when parse fails")
  void shouldReturnDefaultFileOptsWhenParseFails() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "CoreMessages.proto");

    // When / Then
    try (MockedStatic<FileOpts> opts = mockStatic(FileOpts.class)) {
      opts.when(() -> FileOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertThat(OptionUtils.getOpts(descriptor, o -> true))
          .isEqualTo(FileOpts.getDefaultInstance());
    }
  }

  @Test
  @DisplayName("should return default file opts when descriptor is null or has no options")
  void shouldReturnDefaultFileOptsWhenDescriptorIsNullOrHasNoOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_generator_options);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "EmptyMessage.proto");

    // When / Then
    assertThat(OptionUtils.getOpts((FileDescriptor) null, o -> true))
        .isEqualTo(FileOpts.getDefaultInstance());
    assertThat(OptionUtils.getOpts(descriptor, o -> true)).isEqualTo(FileOpts.getDefaultInstance());
  }

  @Test
  @DisplayName("should return message opts when descriptor has options")
  void shouldReturnMessageOptsWhenDescriptorHasOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");

    // When
    MessageOpts optOpts = OptionUtils.getOpts(descriptor, MessageOpts::hasOpt);
    MessageOpts javaOpts = OptionUtils.getOpts(descriptor, MessageOpts::hasJava);

    // Then
    assertThat(optOpts.getOpt().getMethod()).isEqualTo("GET");
    assertThat(javaOpts.getJava().getAnnotationCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("should return default message opts when parse fails")
  void shouldReturnDefaultMessageOptsWhenParseFails() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");

    // When / Then
    try (MockedStatic<MessageOpts> opts = mockStatic(MessageOpts.class)) {
      opts.when(() -> MessageOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertThat(OptionUtils.getOpts(descriptor, o -> true))
          .isEqualTo(MessageOpts.getDefaultInstance());
    }
  }

  @Test
  @DisplayName("should return default message opts when descriptor is null or has no options")
  void shouldReturnDefaultMessageOptsWhenDescriptorIsNullOrHasNoOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_generator_options);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test1");

    // When / Then
    assertThat(OptionUtils.getOpts((Descriptor) null, o -> true))
        .isEqualTo(MessageOpts.getDefaultInstance());
    assertThat(OptionUtils.getOpts(descriptor, o -> true))
        .isEqualTo(MessageOpts.getDefaultInstance());
  }

  @Test
  @DisplayName("should return enum opts when descriptor has options")
  void shouldReturnEnumOptsWhenDescriptorHasOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    EnumDescriptor enumDescriptor = resolveEnum(context.getDescriptors(), "Enum");

    // When
    EnumOpts optOpts = OptionUtils.getOpts(enumDescriptor, EnumOpts::hasOpt);
    EnumOpts javaOpts = OptionUtils.getOpts(enumDescriptor, EnumOpts::hasJava);

    // Then
    assertThat(optOpts).isNotNull();
    assertThat(javaOpts.getJava().getAnnotationCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("should return default enum opts when parse fails")
  void shouldReturnDefaultEnumOptsWhenParseFails() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    EnumDescriptor enumDescriptor = resolveEnum(context.getDescriptors(), "Enum");

    // When / Then
    try (MockedStatic<EnumOpts> opts = mockStatic(EnumOpts.class)) {
      opts.when(() -> EnumOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertThat(OptionUtils.getOpts(enumDescriptor, o -> true))
          .isEqualTo(EnumOpts.getDefaultInstance());
    }
  }

  @Test
  @DisplayName("should return default enum opts when descriptor is null or has no options")
  void shouldReturnDefaultEnumOptsWhenDescriptorIsNullOrHasNoOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_generator_options);
    EnumDescriptor enumDescriptor = resolveEnum(context.getDescriptors(), "Enum");

    // When / Then
    assertThat(OptionUtils.getOpts((EnumDescriptor) null, o -> true))
        .isEqualTo(EnumOpts.getDefaultInstance());
    assertThat(OptionUtils.getOpts(enumDescriptor, o -> true))
        .isEqualTo(EnumOpts.getDefaultInstance());
  }

  @Test
  @DisplayName("should return field opts when descriptor has options")
  void shouldReturnFieldOptsWhenDescriptorHasOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();
    FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);

    // When
    FieldOpts optOpts = OptionUtils.getOpts(fieldDescriptor, FieldOpts::hasOpt);
    FieldOpts tsOpts = OptionUtils.getOpts(fieldDescriptor, FieldOpts::hasTs);

    // Then
    assertThat(optOpts.getOpt().getInQuery()).isTrue();
    assertThat(tsOpts.getTs().getAsString()).isTrue();
  }

  @Test
  @DisplayName("should return default field opts when parse fails")
  void shouldReturnDefaultFieldOptsWhenParseFails() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();
    FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);

    // When / Then
    try (MockedStatic<FieldOpts> opts = mockStatic(FieldOpts.class)) {
      opts.when(() -> FieldOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertThat(OptionUtils.getOpts(fieldDescriptor, o -> true))
          .isEqualTo(FieldOpts.getDefaultInstance());
    }
  }

  @Test
  @DisplayName("should return default field opts when descriptor is null or has no options")
  void shouldReturnDefaultFieldOptsWhenDescriptorIsNullOrHasNoOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test6");
    assertThat(descriptor).isNotNull();
    FieldDescriptor fieldDescriptor = descriptor.getFields().get(0);

    // When / Then
    assertThat(OptionUtils.getOpts((FieldDescriptor) null, o -> true))
        .isEqualTo(FieldOpts.getDefaultInstance());
    assertThat(OptionUtils.getOpts(fieldDescriptor, o -> true))
        .isEqualTo(FieldOpts.getDefaultInstance());
  }

  @Test
  @DisplayName("should return enum value opts when descriptor has options")
  void shouldReturnEnumValueOptsWhenDescriptorHasOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    EnumDescriptor descriptor = resolveEnum(context.getDescriptors(), "Test5");
    assertThat(descriptor).isNotNull();
    EnumValueDescriptor enumValueDescriptor = descriptor.getValues().get(0);

    // When
    EnumValueOpts javaOpts = OptionUtils.getOpts(enumValueDescriptor, EnumValueOpts::hasJava);
    EnumValueOpts optOpts = OptionUtils.getOpts(enumValueDescriptor, EnumValueOpts::hasOpt);

    // Then
    assertThat(javaOpts.getJava().getAnnotationCount()).isZero();
    assertThat(optOpts.getOpt().getValue()).isEqualTo("text1");
  }

  @Test
  @DisplayName("should return default enum value opts when parse fails")
  void shouldReturnDefaultEnumValueOptsWhenParseFails() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    EnumDescriptor descriptor = resolveEnum(context.getDescriptors(), "Test5");
    assertThat(descriptor).isNotNull();
    EnumValueDescriptor enumValueDescriptor = descriptor.getValues().get(0);

    // When / Then
    try (MockedStatic<EnumValueOpts> opts = mockStatic(EnumValueOpts.class)) {
      opts.when(() -> EnumValueOpts.parseFrom((ByteString) any()))
          .thenThrow(new InvalidProtocolBufferException("Invalid"));
      assertThat(OptionUtils.getOpts(enumValueDescriptor, o -> true))
          .isEqualTo(EnumValueOpts.getDefaultInstance());
    }
  }

  @Test
  @DisplayName("should return default enum value opts when descriptor is null or has no options")
  void shouldReturnDefaultEnumValueOptsWhenDescriptorIsNullOrHasNoOptions() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    EnumDescriptor descriptor = resolveEnum(context.getDescriptors(), "Test5");
    assertThat(descriptor).isNotNull();
    EnumValueDescriptor enumValueDescriptor = descriptor.getValues().get(2);

    // When / Then
    assertThat(OptionUtils.getOpts((EnumValueDescriptor) null, o -> true))
        .isEqualTo(EnumValueOpts.getDefaultInstance());
    assertThat(OptionUtils.getOpts(enumValueDescriptor, o -> true))
        .isEqualTo(EnumValueOpts.getDefaultInstance());
  }

  @Test
  @DisplayName("should detect string value enums when opt marks them as string")
  void shouldDetectStringValueEnumsWhenOptMarksThemAsString() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    EnumDescriptor descriptor1 = resolveEnum(context.getDescriptors(), "Test3");
    EnumDescriptor descriptor2 = resolveEnum(context.getDescriptors(), "Test5");
    EnumDescriptor descriptor3 = resolveEnum(context.getDescriptors(), "Enum");

    // When / Then
    assertThat(OptionUtils.isStringValue(descriptor1)).isTrue();
    assertThat(OptionUtils.isStringValue(descriptor2)).isTrue();
    assertThat(OptionUtils.isStringValue(descriptor3)).isFalse();
  }

  @Test
  @DisplayName("should throw when duplicated fields exist")
  void shouldThrowWhenDuplicatedFieldsExist() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_errors);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test2");

    // When / Then
    assertThatThrownBy(() -> OptionUtils.checkDuplicatedFields(descriptor))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(
            "Duplicated field name `Test2.foo` in DuplicatedExtendsFields.proto when extends");
  }

  @Test
  @DisplayName("should not throw when no duplicated fields exist")
  void shouldNotThrowWhenNoDuplicatedFieldsExist() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");

    // When / Then
    assertThatCode(() -> OptionUtils.checkDuplicatedFields(descriptor)).doesNotThrowAnyException();
  }
}
