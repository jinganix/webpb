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

package io.github.jinganix.webpb.gradle

/** User-facing configuration for webpb protobuf code generation. */
open class WebpbExtension {
  /** webpb artifact version; defaults to the Gradle plugin release version. */
  var webpbVersion: String? = null

  /**
   * {@code com.google.protobuf:protoc} version; defaults to the version bundled with the plugin.
   * May also be set via the {@code webpb.protobufVersion} Gradle property.
   */
  var protobufVersion: String? = null

  /** Delete the protobuf output directory before each generation task runs. */
  var cleanOutput: Boolean = true

  /**
   * Absolute path to a local protoc plugin binary. When set, Maven artifacts are not resolved.
   * Useful when developing webpb from source.
   */
  var localPluginPath: String? = null

  /** Source sets to configure; defaults to {@code main} only. */
  var sourceSets: List<String> = listOf("main")
}
