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

package io.github.jinganix.webpb.utilities.utils;

import static io.github.jinganix.webpb.utilities.test.TestUtils.createRequest;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypeFullName;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypePackage;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getFieldTypeSimpleName;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapKeyDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.getMapValueDescriptor;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveEnum;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveFile;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.validation;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.commons.SegmentGroup;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DescriptorUtilsTest {

  @Test
  void shouldCheckIsEnumSuccess() {
    FieldDescriptor fieldDescriptor = mock(FieldDescriptor.class);
    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.ENUM);
    assertTrue(DescriptorUtils.isEnum(fieldDescriptor));

    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.MESSAGE);
    assertFalse(DescriptorUtils.isEnum(fieldDescriptor));
  }

  @Test
  void shouldCheckIsMessageSuccess() {
    FieldDescriptor fieldDescriptor = mock(FieldDescriptor.class);
    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.MESSAGE);
    assertTrue(DescriptorUtils.isMessage(fieldDescriptor));

    when(fieldDescriptor.getJavaType()).thenReturn(FieldDescriptor.JavaType.ENUM);
    assertFalse(DescriptorUtils.isMessage(fieldDescriptor));
  }

  @Test
  void shouldResolveDescriptorSuccess() {
    RequestContext context = createRequest(Dump.test1);
    assertNotNull(resolveMessage(context.getDescriptors(), "Test"));
    assertNull(resolveMessage(context.getDescriptors(), "NotExists"));
  }

  @Test
  void shouldResolveFileDescriptorSuccess() {
    RequestContext context = createRequest(Dump.test1);
    FileDescriptor descriptor = resolveFile(context.getDescriptors(), "Test.proto");
    assertNotNull(descriptor);
    assertNotNull(resolveFile(singletonList(descriptor), "Include.proto"));
    assertNull(resolveFile(context.getDescriptors(), "NotExists"));
  }

  @Test
  void shouldResolveEnumDescriptorSuccess() {
    RequestContext context = createRequest(Dump.test1);
    assertNotNull(resolveEnum(context.getDescriptors(), "Enum"));
    FileDescriptor fileDescriptor =
        context.getDescriptors().stream()
            .filter(x -> "Test.proto".equals(x.getName()))
            .findFirst()
            .orElse(null);
    assertNotNull(resolveEnum(Collections.singletonList(fileDescriptor), "Enum"));
    assertNull(resolveEnum(context.getDescriptors(), "NotExists"));
  }

  @Test
  void shouldGetFieldTypeFilePackageSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    assertNull(getFieldTypePackage(descriptor.getFields().get(0)));
    assertEquals("IncludeProto", getFieldTypePackage(descriptor.getFields().get(1)));
    assertEquals("IncludeProto", getFieldTypePackage(descriptor.getFields().get(2)));
  }

  @Test
  void shouldGetFieldTypeSimpleNameSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    assertEquals("INT32", getFieldTypeSimpleName(descriptor.getFields().get(0)));
    assertEquals("Message", getFieldTypeSimpleName(descriptor.getFields().get(1)));
    assertEquals("Enum", getFieldTypeSimpleName(descriptor.getFields().get(2)));
  }

  @Test
  void shouldGetFieldTypePackageSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    assertNull(getFieldTypePackage(descriptor.getFields().get(0)));
    assertEquals("IncludeProto", getFieldTypePackage(descriptor.getFields().get(1)));
    assertEquals("IncludeProto", getFieldTypePackage(descriptor.getFields().get(2)));
  }

  @Test
  void shouldGetFieldTypeFullNameSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    assertEquals("INT32", getFieldTypeFullName(descriptor.getFields().get(0)));
    assertEquals("IncludeProto.Message", getFieldTypeFullName(descriptor.getFields().get(1)));
    assertEquals("IncludeProto.Enum", getFieldTypeFullName(descriptor.getFields().get(2)));
  }

  @Test
  void shouldGetMapKeyDescriptorSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    assertNotNull(getMapKeyDescriptor(descriptor.getFields().get(4)));
  }

  @Test
  void shouldGetMapValueDescriptorSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    assertNotNull(descriptor);
    assertNotNull(getMapValueDescriptor(descriptor.getFields().get(4)));
  }

  @Test
  void shouldValidationSuccess() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    SegmentGroup group = SegmentGroup.of("/{test1}/{test2.id}");
    assertDoesNotThrow(() -> validation(group, descriptor));
  }

  @Test
  void shouldValidationThrowException() {
    RequestContext context = createRequest(Dump.test1);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "Test");
    SegmentGroup group = SegmentGroup.of("/{test1}/{test2.id}?value={notExists}");
    assertThrows(
        RuntimeException.class, () -> validation(group, descriptor), "Invalid accessor notExists");
  }
}
