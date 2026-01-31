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

import static com.google.protobuf.UnknownFieldSet.Field;
import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.EnumValueOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FieldOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.FileOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptEnumValueOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/** Utilities to handle options. */
public class OptionUtils {

  private OptionUtils() {}

  /**
   * Resolve Webpb {@link FileOpts} from {@link FileDescriptor} with a filter.
   *
   * @param fd {@link FileDescriptor}
   * @param predicate option filter
   * @return Webpb {@link FileOpts}
   */
  public static FileOpts getWebpbOpts(FileDescriptor fd, Predicate<FileOpts> predicate) {
    FileDescriptor wd = DescriptorUtils.resolveFile(fd.getDependencies(), Const.WEBPB_OPTIONS);
    return getOpts(wd, predicate);
  }

  /**
   * Resolve {@link FileOpts} from {@link FileDescriptor} with a filter.
   *
   * @param fileDescriptor {@link FileDescriptor}
   * @param predicate option filter
   * @return {@link FileOpts}
   */
  public static FileOpts getOpts(FileDescriptor fileDescriptor, Predicate<FileOpts> predicate) {
    if (fileDescriptor == null) {
      return FileOpts.getDefaultInstance();
    }
    UnknownFieldSet fieldSet = fileDescriptor.getOptions().getUnknownFields();
    for (Field field : fieldSet.asMap().values()) {
      for (ByteString byteString : field.getLengthDelimitedList()) {
        FileOpts opts;
        try {
          opts = FileOpts.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
          continue;
        }
        if (predicate.test(opts)) {
          return opts;
        }
      }
    }
    return FileOpts.getDefaultInstance();
  }

  /**
   * Resolve {@link MessageOpts} from {@link FileDescriptor} with a filter.
   *
   * @param descriptor {@link Descriptor}
   * @param predicate option filter
   * @return {@link MessageOpts}
   */
  public static MessageOpts getOpts(Descriptor descriptor, Predicate<MessageOpts> predicate) {
    if (descriptor == null) {
      return MessageOpts.getDefaultInstance();
    }
    UnknownFieldSet fieldSet = descriptor.getOptions().getUnknownFields();
    for (Field field : fieldSet.asMap().values()) {
      for (ByteString byteString : field.getLengthDelimitedList()) {
        MessageOpts opts;
        try {
          opts = MessageOpts.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
          continue;
        }
        if (predicate.test(opts)) {
          return opts;
        }
      }
    }
    return MessageOpts.getDefaultInstance();
  }

  /**
   * Resolve {@link EnumOpts} from {@link FileDescriptor} with a filter.
   *
   * @param descriptor {@link EnumDescriptor}
   * @param predicate option filter
   * @return {@link EnumOpts}
   */
  public static EnumOpts getOpts(EnumDescriptor descriptor, Predicate<EnumOpts> predicate) {
    if (descriptor == null) {
      return EnumOpts.getDefaultInstance();
    }
    UnknownFieldSet fieldSet = descriptor.getOptions().getUnknownFields();
    for (Field field : fieldSet.asMap().values()) {
      for (ByteString byteString : field.getLengthDelimitedList()) {
        EnumOpts opts;
        try {
          opts = EnumOpts.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
          continue;
        }
        if (predicate.test(opts)) {
          return opts;
        }
      }
    }
    return EnumOpts.getDefaultInstance();
  }

  /**
   * Resolve {@link FieldOpts} from {@link FileDescriptor} with a filter.
   *
   * @param descriptor {@link FileDescriptor}
   * @param predicate option filter
   * @return {@link FieldOpts}
   */
  public static FieldOpts getOpts(FieldDescriptor descriptor, Predicate<FieldOpts> predicate) {
    if (descriptor == null) {
      return FieldOpts.getDefaultInstance();
    }
    UnknownFieldSet fieldSet = descriptor.getOptions().getUnknownFields();
    for (Field field : fieldSet.asMap().values()) {
      for (ByteString byteString : field.getLengthDelimitedList()) {
        FieldOpts opts;
        try {
          opts = FieldOpts.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
          continue;
        }
        if (predicate.test(opts)) {
          return opts;
        }
      }
    }
    return FieldOpts.getDefaultInstance();
  }

  /**
   * Resolve {@link EnumValueOpts} from {@link EnumValueDescriptor} with a filter.
   *
   * @param descriptor {@link EnumValueDescriptor}
   * @param predicate option filter
   * @return {@link EnumValueOpts}
   */
  public static EnumValueOpts getOpts(
      EnumValueDescriptor descriptor, Predicate<EnumValueOpts> predicate) {
    if (descriptor == null) {
      return EnumValueOpts.getDefaultInstance();
    }
    UnknownFieldSet fieldSet = descriptor.getOptions().getUnknownFields();
    for (Field field : fieldSet.asMap().values()) {
      for (ByteString byteString : field.getLengthDelimitedList()) {
        EnumValueOpts opts;
        try {
          opts = EnumValueOpts.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
          continue;
        }
        if (predicate.test(opts)) {
          return opts;
        }
      }
    }
    return EnumValueOpts.getDefaultInstance();
  }

  /**
   * If this enum use string value.
   *
   * @param descriptor {@link EnumDescriptor}
   * @return true if use string value
   */
  public static boolean isStringValue(EnumDescriptor descriptor) {
    EnumOpts enumOpts = getOpts(descriptor, EnumOpts::hasOpt);
    if (enumOpts.getOpt().getStringValue()) {
      return true;
    }
    for (Descriptors.EnumValueDescriptor valueDescriptor : descriptor.getValues()) {
      OptEnumValueOpts opts = getOpts(valueDescriptor, EnumValueOpts::hasOpt).getOpt();
      if (!StringUtils.isEmpty(opts.getValue())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get extended messages of a message.
   *
   * @param descriptor {@link Descriptor}
   * @return extended messages
   */
  public static List<Descriptor> getExtendedMessages(Descriptor descriptor) {
    List<Descriptor> descriptors = new ArrayList<>();
    do {
      OptMessageOpts messageOpts = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
      if (!messageOpts.hasExtends()) {
        Collections.reverse(descriptors);
        return descriptors;
      }
      descriptor =
          resolveMessage(Collections.singletonList(descriptor.getFile()), messageOpts.getExtends());
      if (descriptor != null) {
        descriptors.add(descriptor);
      }
    } while (true);
  }

  /**
   * Throw an exception when there are duplicated fields.
   *
   * @param descriptor {@link Descriptor}
   */
  public static void checkDuplicatedFields(Descriptor descriptor) {
    List<FieldDescriptor> fieldDescriptors = OptionUtils.getAllFields(descriptor);
    Set<String> fieldNames = new HashSet<>();
    for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
      if (fieldNames.contains(fieldDescriptor.getName())) {
        throw new RuntimeException(
            String.format(
                "Duplicated field name `%s.%s` in %s when extends",
                descriptor.getName(),
                fieldDescriptor.getName(),
                fieldDescriptor.getFile().getFullName()));
      }
      fieldNames.add(fieldDescriptor.getName());
    }
  }

  /**
   * Get fields of message and the extended message.
   *
   * @param descriptor {@link Descriptor}
   * @return all fields
   */
  public static List<FieldDescriptor> getAllFields(Descriptor descriptor) {
    return Stream.of(getExtendedMessages(descriptor), Collections.singletonList(descriptor))
        .flatMap(List::stream)
        .map(Descriptor::getFields)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }
}
