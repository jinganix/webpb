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

import static io.github.jinganix.webpb.utilities.test.TestUtils.createRequest;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypeFullName;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypePackage;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypeSimpleName;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getGenericDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapKeyDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapValueDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveEnum;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveEnumValue;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveFile;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveNestedTypes;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveTopLevelTypes;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.validation;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.commons.SegmentGroup;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DescriptorUtils")
class DescriptorUtilsTest {

  @Test
  @DisplayName("should return true for enum fields when java type is enum")
  void shouldReturnTrueForEnumFieldsWhenJavaTypeIsEnum() {
    // Given
    FieldDescriptor fieldDescriptor = mock(FieldDescriptor.class);
    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.ENUM);

    // When / Then
    assertThat(DescriptorUtils.isEnum(fieldDescriptor)).isTrue();

    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.MESSAGE);
    assertThat(DescriptorUtils.isEnum(fieldDescriptor)).isFalse();
  }

  @Test
  @DisplayName("should return true for message fields when java type is message")
  void shouldReturnTrueForMessageFieldsWhenJavaTypeIsMessage() {
    // Given
    FieldDescriptor fieldDescriptor = mock(FieldDescriptor.class);
    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.MESSAGE);

    // When / Then
    assertThat(DescriptorUtils.isMessage(fieldDescriptor)).isTrue();

    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.ENUM);
    assertThat(DescriptorUtils.isMessage(fieldDescriptor)).isFalse();
  }

  @Test
  @DisplayName("should resolve message descriptor when name exists")
  void shouldResolveMessageDescriptorWhenNameExists() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);

    // When / Then
    assertThat(resolveMessage(context.getDescriptors(), "Test")).isNotNull();
    assertThat(resolveMessage(context.getDescriptors(), "NotExists")).isNull();
  }

  @Test
  @DisplayName("should resolve file descriptor when name exists")
  void shouldResolveFileDescriptorWhenNameExists() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "CoreMessages.proto");

    // When / Then
    assertThat(descriptor).isNotNull();
    assertThat(resolveFile(singletonList(descriptor), "Include.proto")).isNotNull();
    assertThat(resolveFile(context.getDescriptors(), "NotExists")).isNull();
  }

  @Test
  @DisplayName("should resolve enum descriptor when name exists")
  void shouldResolveEnumDescriptorWhenNameExists() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    FileDescriptor fileDescriptor =
        context.getDescriptors().stream()
            .filter(x -> "CoreMessages.proto".equals(x.getName()))
            .findFirst()
            .orElse(null);

    // When / Then
    assertThat(resolveEnum(context.getDescriptors(), "Enum")).isNotNull();
    assertThat(resolveEnum(Collections.singletonList(fileDescriptor), "Enum")).isNotNull();
    assertThat(resolveEnum(context.getDescriptors(), "NotExists")).isNull();
  }

  @Test
  @DisplayName("should return field type package when field references external types")
  void shouldReturnFieldTypePackageWhenFieldReferencesExternalTypes() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();

    // When / Then
    assertThat(getFieldTypePackage(descriptor.getFields().get(0))).isNull();
    assertThat(getFieldTypePackage(descriptor.getFields().get(1))).isEqualTo("IncludeProto");
    assertThat(getFieldTypePackage(descriptor.getFields().get(2))).isEqualTo("IncludeProto");
  }

  @Test
  @DisplayName("should return field type simple name when field has type")
  void shouldReturnFieldTypeSimpleNameWhenFieldHasType() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();

    // When / Then
    assertThat(getFieldTypeSimpleName(descriptor.getFields().get(0))).isEqualTo("INT32");
    assertThat(getFieldTypeSimpleName(descriptor.getFields().get(1))).isEqualTo("Message");
    assertThat(getFieldTypeSimpleName(descriptor.getFields().get(2))).isEqualTo("Enum");
  }

  @Test
  @DisplayName("should return field type full name when field has type")
  void shouldReturnFieldTypeFullNameWhenFieldHasType() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();

    // When / Then
    assertThat(getFieldTypeFullName(descriptor.getFields().get(0))).isEqualTo("INT32");
    assertThat(getFieldTypeFullName(descriptor.getFields().get(1)))
        .isEqualTo("IncludeProto.Message");
    assertThat(getFieldTypeFullName(descriptor.getFields().get(2))).isEqualTo("IncludeProto.Enum");
  }

  @Test
  @DisplayName("should return map key descriptor when field is a map")
  void shouldReturnMapKeyDescriptorWhenFieldIsAMap() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();

    // When / Then
    assertThat(getMapKeyDescriptor(descriptor.getFields().get(4))).isNotNull();
  }

  @Test
  @DisplayName("should return map value descriptor when field is a map")
  void shouldReturnMapValueDescriptorWhenFieldIsAMap() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertThat(descriptor).isNotNull();

    // When / Then
    assertThat(getMapValueDescriptor(descriptor.getFields().get(4))).isNotNull();
  }

  @Test
  @DisplayName("should not throw when segment group accessors are valid")
  void shouldNotThrowWhenSegmentGroupAccessorsAreValid() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    SegmentGroup group = SegmentGroup.of("/{test1}/{test2.id}");

    // When / Then
    assertThatCode(() -> validation(group, descriptor)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("should resolve nested and top level descriptors when dump is valid")
  void shouldResolveNestedAndTopLevelDescriptorsWhenDumpIsValid() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    FileDescriptor fileDescriptor = context.getDescriptors().get(0);

    // When / Then
    assertThat(resolveTopLevelTypes(fileDescriptor)).isNotEmpty();
    assertThat(resolveNestedTypes(resolveMessage(context.getDescriptors(), "Test"))).isNotEmpty();
    assertThat(
            getGenericDescriptor(
                resolveMessage(context.getDescriptors(), "Test").getFields().get(1)))
        .isNotNull();
    assertThat(resolveEnumValue(context.getDescriptors(), "NotExists")).isNull();
  }

  @Test
  @DisplayName("should throw when segment group references invalid accessor")
  void shouldThrowWhenSegmentGroupReferencesInvalidAccessor() {
    // Given
    RequestContext context = createRequest(Dump.proto2_core_codegen);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    SegmentGroup group = SegmentGroup.of("/{test1}/{test2.id}?value={notExists}");

    // When / Then
    assertThatThrownBy(() -> validation(group, descriptor))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Invalid accessor notExists");
  }
}
