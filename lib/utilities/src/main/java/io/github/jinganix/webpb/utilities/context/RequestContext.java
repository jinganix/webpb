/*
 * Copyright (c) 2020 jinganix@gmail.com, All Rights Reserved.
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
 */

package io.github.jinganix.webpb.utilities.context;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/** Context wrapper for generator. */
@Getter
public class RequestContext {

  private List<FileDescriptor> descriptors;

  private List<FileDescriptor> targetDescriptors;

  /**
   * Create context with a file option filter.
   *
   * @throws Exception if eny exceptions
   */
  public RequestContext() throws Exception {
    CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);
    initDescriptors(request);
  }

  private void initDescriptors(CodeGeneratorRequest request) throws DescriptorValidationException {
    Map<String, FileDescriptor> filesMap = new HashMap<>();
    for (DescriptorProtos.FileDescriptorProto proto : request.getProtoFileList()) {
      FileDescriptor[] dependencies =
          proto.getDependencyList().stream().map(filesMap::get).toArray(FileDescriptor[]::new);

      FileDescriptor descriptor = FileDescriptor.buildFrom(proto, dependencies);
      filesMap.put(proto.getName(), descriptor);
    }
    descriptors = new ArrayList<>(filesMap.values());
    targetDescriptors =
        request.getFileToGenerateList().stream().map(filesMap::get).collect(Collectors.toList());
  }
}
