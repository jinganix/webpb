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

package utils

import org.gradle.api.Project
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

object Vers {
  private var initialized = false

  lateinit var versionAssertj: String
  lateinit var versionCommonsIo: String
  lateinit var versionCommonsLang3: String
  lateinit var versionCompileTesting: String
  lateinit var versionDependencyManagementPlugin: String
  lateinit var versionFreemarker: String
  lateinit var versionGoogleJavaFormat: String
  lateinit var versionGradleMavenPublishPlugin: String
  lateinit var versionGradleVersionsPlugin: String
  lateinit var versionJackson: String
  lateinit var versionJacksonAnnotations: String
  lateinit var versionJacoco: String
  lateinit var versionJakartaServletApi: String
  lateinit var versionJakartaXml: String
  lateinit var versionJavaParser: String
  lateinit var versionJavaxServletApi: String
  lateinit var versionJupiter: String
  lateinit var versionLombok: String
  lateinit var versionMockitoCore: String
  lateinit var versionMockitoInline: String
  lateinit var versionNetty: String
  lateinit var versionProtobufGradlePlugin: String
  lateinit var versionProtobufJava: String
  lateinit var versionReactorNetty: String
  lateinit var versionSpotlessPluginGradle: String
  lateinit var versionSpringBootGradlePlugin: String
  lateinit var versionSpringFramework: String
  lateinit var webpb: String

  fun initialize(project: Project, override: Properties) {
    if (initialized) {
      return
    }
    this.webpb = project.version.toString()
    this::class.memberProperties.forEach {
      if (it !is KMutableProperty<*>) {
        return
      }
      val key = it.name
      if (override.containsKey(key)) {
        it.setter.call(this, override.getProperty(key))
      } else if (project.hasProperty(key)) {
        it.setter.call(this, project.property(key))
      }
    }
    initialized = true
  }
}
