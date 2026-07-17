[![CI](https://github.com/jinganix/webpb/actions/workflows/ci.yml/badge.svg)](https://github.com/jinganix/webpb/actions/workflows/ci.yml)
[![lib-commons coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=lib-commons)](https://codecov.io/gh/jinganix/webpb/flags/lib-commons)
[![lib-utilities coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=lib-utilities)](https://codecov.io/gh/jinganix/webpb/flags/lib-utilities)
[![plugin coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=plugin)](https://codecov.io/gh/jinganix/webpb/flags/plugin)
[![runtime-java coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=runtime-java)](https://codecov.io/gh/jinganix/webpb/flags/runtime-java)
[![runtime-processor coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=runtime-processor)](https://codecov.io/gh/jinganix/webpb/flags/runtime-processor)
[![sample-backend coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=sample-backend)](https://codecov.io/gh/jinganix/webpb/flags/sample-backend)
[![frontend coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=frontend)](https://codecov.io/gh/jinganix/webpb/flags/frontend)
[![runtime-ts coverage](https://codecov.io/gh/jinganix/webpb/branch/master/graph/badge.svg?flag=runtime-ts)](https://codecov.io/gh/jinganix/webpb/flags/runtime-ts)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[English Version](README.md)

# webpb

从 Protocol Buffers 生成 Web API 模型与路由元数据。

一份 `.proto` 即可描述请求/响应类型、HTTP 方法、路径模板、校验注解和 JSON 字段命名。protoc 插件会生成：

- **Java** — 实现 `WebpbMessage` 的 POJO、Jackson/校验注解，以及通过注解处理器接入 Spring `@WebpbRequestMapping`。
- **TypeScript** — 带 `webpbMeta()`、`create()`、`fromAlias()` 的类型化 HTTP 客户端类。

Proto 扩展定义见 [`lib/proto/src/main/resources/webpb/WebpbExtend.proto`](lib/proto/src/main/resources/webpb/WebpbExtend.proto)。

## 示例项目

[`sample/`](sample/) 是一个端到端的 webpb option 演示。

| 模块 | 说明 |
|------|------|
| [`sample/proto`](sample/proto) | 共享 `.proto` 与 `WebpbOptions.proto`（全局 Java/TS 默认配置） |
| [`sample/backend`](sample/backend) | Spring Boot 应用；支持 Gradle 或 Maven；从 proto 生成 Java |
| [`sample/frontend`](sample/frontend) | Vite + React + TypeScript 应用；通过 `webpb generate` 从 proto 生成 TS |

### 定义 HTTP 接口

请求消息在 `(m_opts).opt` 中携带路由元数据。路径段使用 `{field}` 或 `{nested.field}` 占位；查询参数字段用 `(opts).opt = {in_query: true}` 标记。

```protobuf
message QueryParamRequest {
  option (m_opts).opt = {
    method: "GET"
    path: "/options/query?tag={tag}"
  };

  required string tag = 1 [(opts).opt = {in_query: true}];
}
```

### Java 控制器

生成的类型实现 `WebpbMessage`。在控制器方法上加 `@WebpbRequestMapping`，注解处理器会根据请求消息的元数据填充 `@RequestMapping`。

```java
@RestController
public class OptionsController {

  @WebpbRequestMapping
  public QueryParamResponse queryParam(@Valid QueryParamRequest request) {
    // ...
  }

  @WebpbRequestMapping
  public HttpRouteResponse httpRoute(@Valid @RequestBody HttpRouteRequest request) {
    // ...
  }
}
```

在 Spring MVC 配置中注册 `WebpbHandlerMethodArgumentResolver` 与 `WebpbRequestBodyAdvice`（见 [`WebMvcConfiguration.java`](sample/backend/src/main/java/io/github/jinganix/webpb/sample/backend/WebMvcConfiguration.java)）。

### TypeScript 客户端

生成的类通过 `webpbMeta()` 暴露 `method` 与 `path`。示例 [`HttpService`](sample/frontend/src/services/http.service.ts) 据此发送 JSON 请求：

```typescript
this.httpService.request(
  QueryParamRequest.create({ tag: "demo" }),
  QueryParamResponse,
);
```

### 全局选项（`WebpbOptions.proto`）

项目级默认配置放在共享的 `WebpbOptions.proto` 中，由每个 proto 文件 import。示例中为 Java 声明校验注解 import，并设置 TS 默认值：

```protobuf
option (f_opts).java = {
  import: 'jakarta.validation.Valid'
  import: 'jakarta.validation.constraints.NotNull'
  // ...
};

option (f_opts).ts = {
  int64_as_string: false
};
```

示例中字段级 Jackson 命名通过 message 选项与占位符配置：

```protobuf
option (m_opts).java = {field_annotation: '@JsonProperty("{{_ALIAS_}}")'};
option (m_opts).ts = {auto_alias: true};
```

## Proto 选项参考

选项可挂在 file、message、enum、field 或 enum value 级别。每个 proto 文件需 import `webpb/WebpbExtend.proto`。

### 文件 — `(f_opts)`

| 分组 | 字段 | 说明 |
|------|------|------|
| `java` | `gen_getter` | 生成 getter（默认 `true`） |
| `java` | `gen_setter` | 生成 setter（默认 `true`） |
| `java` | `import` | 为生成代码追加 Java import |
| `java` | `annotation` | 每个生成类型的类级注解 |
| `java` | `field_annotation` | 字段注解；支持 `{{_ALIAS_}}`、`{{_FIELD_NAME_}}` |
| `java` | `repeatable_annotation` | 允许重复出现的注解全限定名 |
| `ts` | `import` | 追加 TypeScript import |
| `ts` | `int64_as_string` | JSON 中将 `int64` 序列化为字符串 |
| `ts` | `auto_alias` | 由 proto 字段名推导 JSON 字段名 |
| `ts` | `default_const_enum` | 为 protobuf 枚举生成 const enum |
| `ts` | `enum_auto_alias` | 生成次级运行时别名枚举（`EnumX` / `ConstX`）；默认 `true` |
| `ts` | `enum_values_literal` | `XValues` 输出为数字字面量（`[0, 1, 2]`）而非成员引用；默认 `false` |
| `ts` | `enum_by_name` | 生成 `XByName` 正向映射（名 → 数）；默认 `false` |
| `ts` | `enum_by_value` | 生成 `XByValue` 反向映射（数 → 名）；默认 `false` |
| `ts` | `enum_helpers` | 在存在 `by_name` / `by_value` 映射时生成 `xFromName` / `xToName` 辅助函数；默认 `false` |
| `ts` | `enum_emit_mode` | 枚举输出形态：`ts`（默认）、`js_dts`、`ts_and_js_dts` |

### 消息 — `(m_opts)`

| 分组 | 字段 | 说明 |
|------|------|------|
| `opt` | `method` | 请求消息的 HTTP 方法（如 `GET`、`POST`） |
| `opt` | `path` | 带 `{field}` 占位符的 URL 模板 |
| `opt` | `context` | 基础路径前缀 |
| `opt` | `extends` | Java 父类名 |
| `opt` | `implements` | 要实现的 Java 接口 |
| `opt` | `sub_type` | 多态消息的子类型鉴别字段 |
| `opt` | `sub_values` | 允许的子类型值 |
| `opt` | `alias_reserve` | 为子消息预留的 alias 编号上限，必须大于本消息最大 field id；子消息字段 alias 序号为 `max(祖先已用最大序号, alias_reserve) + field id - 1` |
| `java` | `annotation` | 类级 Java 注解 |
| `java` | `field_annotation` | 该消息所有字段的默认字段注解 |
| `ts` | `auto_alias` | 覆盖文件级 `auto_alias` |

### 字段 — `(opts)`

| 分组 | 字段 | 说明 |
|------|------|------|
| `opt` | `omitted` | 从生成 API 中排除该字段 |
| `opt` | `in_query` | 绑定到查询字符串（用于 GET / 路径模板） |
| `java` | `annotation` | Java 字段注解（如 `@NotNull`、`@Pattern(...)`） |
| `java` | `as_set` | 重复字段生成为 `Set<T>` 而非 `List<T>` |
| `java` | `as_collection` | 重复字段生成为 `Collection<T>` 而非 `List<T>` |
| `ts` | `as_string` | 将数值字段序列化为字符串 |
| `ts` | `alias` | JSON 属性名覆盖 |
| `ts` | `auto_alias` | 覆盖该字段的 alias 行为 |

重复字段默认为 `List<T>`。可通过 Java 字段选项修改集合类型：

```protobuf
repeated string tags = 1 [(opts).java = {as_set: true}];
repeated int32 ids = 2 [(opts).java = {as_collection: true}];
```

### 枚举 — `(e_opts)`

| 分组 | 字段 | 说明 |
|------|------|------|
| `opt` | `string_value` | 使用字符串值而非数字序号 |
| `java` | `annotation` | 枚举级 Java 注解 |
| `java` | `implements` | 枚举实现的 Java 接口 |
| `ts` | `default_const_enum` | 覆盖文件级 const enum 行为 |
| `ts` | `enum_auto_alias` | 覆盖文件级 `enum_auto_alias` |
| `ts` | `enum_values_literal` | 覆盖文件级 `enum_values_literal` |
| `ts` | `enum_by_name` | 覆盖文件级 `enum_by_name` |
| `ts` | `enum_by_value` | 覆盖文件级 `enum_by_value` |
| `ts` | `enum_helpers` | 覆盖文件级 `enum_helpers` |
| `ts` | `enum_emit_mode` | 覆盖文件级 `enum_emit_mode` |

#### TypeScript 枚举输出

启用 `default_const_enum` 时，webpb 会生成主枚举 `const enum X`，以及次级运行时别名（`enum EnumX` 或 `const enum ConstX`，取决于主枚举类型）。该双向别名在 esbuild、SWC、Rolldown 等打包器中未经 `tsc` 内联时会显著增大 bundle 体积。

上述 `enum_*` 选项为按需开启的调优项。可在 `(f_opts).ts` 中设置文件级默认值，或在 `(e_opts).ts` 中覆盖单个枚举。解析顺序：枚举 → 文件 → `WebpbOptions.proto`。

前端 bundle 体积优化推荐配置：

```protobuf
option (f_opts).ts = {
  default_const_enum: true
  enum_emit_mode: "js_dts"
  enum_auto_alias: false
  enum_values_literal: true
  enum_by_name: false
};
```

仅在需要运行时名↔数查询的枚举上开启 `enum_by_name` 或 `enum_by_value`（如解析配置字符串、日志）。全局 `enum_by_name: true` 会把每个成员名作为字符串键打进 bundle。

```protobuf
enum Bar {
  option (e_opts).ts = { enum_by_name: true };
  // ...
}
```

`Foo` 生成示例（单 enum 开启 `enum_by_name: true` 时）：

```typescript
export const enum Foo {
  a = 0,
  b = 1,
  c = 2,
}

export const FooValues: readonly Foo[] = [0, 1, 2];

export const FooByName = {
  a: 0,
  b: 1,
  c: 2,
} as const;

export type FooName = keyof typeof FooByName;

export function fooFromName(name: FooName): Foo {
  return FooByName[name];
}
```

启用 `enum_helpers: true` 时，会生成窄查找函数（`enum_by_name` 开启时输出 `xFromName`，`enum_by_value` 开启时输出 `xToName`），作为旧版 `EnumX[name]` / `EnumX[code]` 的类型安全替代。

启用 `enum_values_literal` 时，`Values` 类型为 `readonly X[]`，`for...of` 无需 `as X[]` 强转。映射表使用 `as const`，并导出 `XName` / `XByValueKey` 辅助类型。

引用其他 proto 文件中枚举的 message 字段使用 `import type { X }`，而非 `import * as XEnum`，避免 message 模块 transitive 依赖枚举的运行时导出（`Values`、`ByName`）。多态消息的 `sub_values` 等仍需枚举成员的运行时访问时，仍保留 namespace import。

#### `enum_emit_mode: js_dts`

设为 `js_dts`（或迁移期 `ts_and_js_dts`）时，每个枚举单独输出 `{EnumName}.d.ts` + `{EnumName}.js`。`{Package}.ts` 变为 **shim**：从 `./{EnumName}` re-export 类型、从 `./{EnumName}.js` re-export 运行时值，**不再**内联第二份 `const enum`。`.d.ts` 含类型与 `declare` 绑定；`.js` 仅含运行时值。打包器可直接消费 `.js`，无需 `tsc` 内联。

`ts_and_js_dts` 与 `js_dts` 生成物**相同**（shim + split，无双份 inline enum），仅作迁移别名；新项目请用 `js_dts`。

```protobuf
option (f_opts).ts = {
  default_const_enum: true
  enum_emit_mode: "js_dts"
  enum_auto_alias: false
  enum_values_literal: true
};
```

`Foo.d.ts`：

```typescript
export const enum Foo {
  a = 0,
  b = 1,
  c = 2,
}

export declare const FooValues: readonly Foo[];
```

`Foo.js`：

```typescript
export const FooValues = [0, 1, 2];
```

当 protobuf **package 名与枚举名不同**（如 `package BarEnum` + `enum Bar`）时，`{Package}.ts` 是稳定入口，兼容 `import { Bar } from "./BarEnum"`：

```typescript
// BarEnum.ts（shim）
export type { Bar } from "./Bar";
export { BarValues } from "./Bar.js";
```

纯枚举 proto 也会生成 `{Package}.ts`（shim）。同文件 message 字段直接使用 shim 中的类型，无需额外 import。跨文件引用使用 `import type { Bar } from "./BarEnum"`（package shim），而非 `./Bar.js`，保证手写代码与生成 message 共用同一 `Bar` 类型。

#### 稀疏枚举值

`XValues` 按 **proto 声明顺序** 列出所有已声明的数值，不是连续的 `[0..max]` 区间。例如成员为 `a = 0`、`b = 20`、`c = 23` 时，`XValues` 为 `[0, 20, 23]`。仅在需要运行时名↔值查询时开启 `enum_by_name` / `enum_by_value`；webpb 默认不生成 `min`/`max` 或 `isValid(n)` 辅助函数。

#### 从 `EnumX` 迁移

| 原写法 | 新写法 |
|--------|--------|
| `import { EnumX, X }` | `import { X, XByName }` 或 `import type { X }` |
| `EnumX[name]` | `XByName[name]`，或 `enum_helpers: true` 时用 `xFromName(name)` |
| `EnumX[code]` | `enum_by_value: true` 时用 `xToName(code)` 或 `XByValue[code]` |
| `Object.entries(EnumX)` | `Object.entries(XByName)` |

迁移期间可临时设 `enum_auto_alias: true`（或保留旧默认），待调用方不再引用 `EnumX` 后再关闭。

### 枚举值 — `(v_opts)`

| 分组 | 字段 | 说明 |
|------|------|------|
| `opt` | `value` | 启用 `string_value` 时的字符串表示 |
| `java` | `annotation` | 每个枚举值的 Java 注解 |

## 快速开始

已发布产物（Maven Central，group `io.github.jinganix.webpb`）：

| 构件 | 用途 |
|------|------|
| `webpb-gradle-plugin` | Gradle 约定插件 |
| `webpb-protoc-java` | Java protoc 插件（`:all` fat jar 或平台二进制） |
| `webpb-protoc-ts` | TypeScript protoc 插件（`:all` fat jar 或平台二进制） |
| `webpb-proto` | `WebpbExtend.proto` 及 well-known 类型，供 `-I` / `protobuf(...)` 依赖 |
| `webpb-runtime` | Java 运行时库 |
| `webpb-processor` | Spring `@WebpbRequestMapping` 注解处理器 |
| [`webpb` (npm)](https://www.npmjs.com/package/webpb) | TypeScript 运行时与 `webpb generate` CLI |

每个 `.proto` 必须 import webpb 扩展：

```protobuf
import "webpb/WebpbExtend.proto";
```

在 Gradle/Maven 中添加 `webpb-proto` 依赖，或通过 `-I` 指定包含 `webpb/` 目录的路径。

### Gradle

配合 [protobuf Gradle 插件](https://github.com/google/protobuf-gradle-plugin) 使用 webpb 约定插件：

**Java**（[`sample/backend`](sample/backend/build.gradle.kts) / [`pom.xml`](sample/backend/pom.xml)）：

```kotlin
plugins {
  id("com.google.protobuf") version "0.9.6"
  id("io.github.jinganix.webpb.java") version "0.0.32"
}

dependencies {
  protobuf("io.github.jinganix.webpb:webpb-proto:0.0.32")
  implementation("io.github.jinganix.webpb:webpb-runtime:0.0.32")
  annotationProcessor("io.github.jinganix.webpb:webpb-processor:0.0.32")
  protobuf(project(":your-proto-module"))
}
```

**TypeScript**：

```kotlin
plugins {
  id("com.google.protobuf") version "0.9.6"
  id("io.github.jinganix.webpb.ts") version "0.0.32"
}

dependencies {
  protobuf("io.github.jinganix.webpb:webpb-proto:0.0.32")
}
```

可选配置：

```kotlin
webpb {
  webpbVersion = "0.0.32"      // 默认与 Gradle 插件版本一致
  protobufVersion = "4.35.1"   // com.google.protobuf:protoc 版本
  cleanOutput = false          // 默认 true：生成前删除输出目录
  localPluginPath = "/path/to/webpb-protoc-java" // 跳过 Maven 解析
}
```

约定插件会自动配置 `protoc`、从 Maven 解析 `webpb-protoc-*:all@jar`、移除内置 `java` 生成器并绑定 `generateProto` 任务。在 webpb 源码仓库内开发时，会自动使用 `plugin/bin/` 下的本地二进制。

也可手动配置（不使用约定插件）：

```kotlin
protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:4.35.1"
  }
  plugins {
    id("ts") {
      artifact = "io.github.jinganix.webpb:webpb-protoc-ts:0.0.32:all@jar"
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.builtins { remove("java") }
      it.plugins { id("ts") }
    }
  }
}
```

### Maven

使用 [protobuf-maven-plugin](https://www.xolstice.org/protobuf-maven-plugin/) 配合已发布的 `webpb-protoc-java`（`classifier: all`）。完整示例见 [`sample/backend/pom.xml`](sample/backend/pom.xml)。

```xml
<properties>
  <webpb.version>0.0.32</webpb.version>
  <protobuf.version>4.35.1</protobuf.version>
</properties>

<dependencies>
  <dependency>
    <groupId>io.github.jinganix.webpb</groupId>
    <artifactId>webpb-proto</artifactId>
    <version>${webpb.version}</version>
  </dependency>
  <dependency>
    <groupId>io.github.jinganix.webpb</groupId>
    <artifactId>webpb-runtime</artifactId>
    <version>${webpb.version}</version>
  </dependency>
</dependencies>

<build>
  <extensions>
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.7.1</version>
    </extension>
  </extensions>
  <plugins>
    <plugin>
      <groupId>org.xolstice.maven.plugins</groupId>
      <artifactId>protobuf-maven-plugin</artifactId>
      <version>0.6.1</version>
      <configuration>
        <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
        <protocPlugins>
          <protocPlugin>
            <id>webpb</id>
            <groupId>io.github.jinganix.webpb</groupId>
            <artifactId>webpb-protoc-java</artifactId>
            <version>${webpb.version}</version>
            <classifier>all</classifier>
            <mainClass>io.github.jinganix.webpb.java.Main</mainClass>
          </protocPlugin>
        </protocPlugins>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>compile-custom</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

执行 `mvn generate-sources` 在 `target/generated-sources/protobuf/` 下生成 Java。Spring MVC 需额外添加 `webpb-processor` 注解处理器依赖。

### npm / TypeScript

安装运行时（开发依赖），使用内置 CLI 生成 TypeScript：

```shell
npm install -D webpb
```

`webpb` CLI 会自动解析 `protoc` 与 `webpb-protoc-ts`，内置 `webpb/WebpbExtend.proto` include 路径。每个 protobuf 包生成一个 `.ts` 文件（如 `OptionsProto.ts`）。

```shell
npx webpb generate \
  -o src/generated \
  -I path/to/your/protos \
  path/to/your/protos/*.proto
```

常用参数：

| 参数 | 说明 |
|------|------|
| `-o, --out` | 输出目录（`--ts_out`），必填 |
| `-I, --include` | 额外 proto include 路径（可重复） |
| `--no-webpb-proto` | 跳过内置 webpb proto include |
| `--protoc` | `protoc` 路径（默认：`PATH`，否则从 Maven Central 下载） |
| `--plugin` | `webpb-protoc-ts` 路径（默认：从 GitHub Releases 下载） |
| `--webpb-version` | 插件 release 版本 |
| `--protobuf-version` | `protoc` 版本（默认与 Gradle 插件一致） |

环境变量：`PROTOC`、`WEBPB_PROTOC_TS`、`WEBPB_VERSION`、`WEBPB_PROTOBUF_VERSION`。

工具解析顺序：

**`webpb-protoc-ts`：** `WEBPB_PROTOC_TS` → 源码仓库 `plugin/bin/`（开发 webpb 时）→ 缓存的 GitHub release（`~/.cache/webpb/{version}/`）→ 下载。

**`protoc`：** `PROTOC` → `PATH` → 缓存的 Maven Central 下载（`~/.cache/webpb/protoc/{version}/`）。

内置 proto include 随 npm 包发布（`node_modules/webpb/proto`）。若自行提供 `-I` 路径，可使用 `--no-webpb-proto`。

在 `package.json` 中添加：

```json
{
  "scripts": {
    "proto": "webpb generate -o src/generated -I protos protos/*.proto"
  }
}
```

示例前端（使用本地 `runtime/ts/build` 中的 `webpb` 包；从源码开发时需先在 `runtime/ts` 执行 `npm run build`）：

```shell
cd sample/frontend
npm install
npm run proto
```

如需手动调用 `protoc`，可从 [GitHub Releases](https://github.com/jinganix/webpb/releases) 下载 `webpb-protoc-ts-<platform>` 或设置 `WEBPB_PROTOC_TS`。详见下方 [protoc 命令行](#protoc-命令行任意构建工具)。

### protoc 命令行（任意构建工具）

webpb 插件是标准 [protoc](https://grpc.io/docs/protoc-installation/) 插件：从 stdin 读取 `CodeGeneratorRequest`，写入 `--*_out`。

**获取插件二进制**

- [GitHub Releases](https://github.com/jinganix/webpb/releases) — `webpb-protoc-java-<platform>` / `webpb-protoc-ts-<platform>`
- Maven Central — `io.github.jinganix.webpb:webpb-protoc-java:VERSION:all`（通过 `java -jar` 运行，或由 Gradle/Maven 自动解析）
- 源码构建（需要 [Go](https://go.dev/)）：`cd plugin && make build`

**生成 Java**（插件名 `webpb`）：

```shell
protoc \
  -I path/to/webpb-proto-include \
  -I path/to/your/protos \
  --plugin=protoc-gen-webpb=/path/to/webpb-protoc-java \
  --webpb_out=path/to/output \
  path/to/your/protos/*.proto
```

**生成 TypeScript**（插件名 `ts`）：

```shell
protoc \
  -I path/to/webpb-proto-include \
  -I path/to/your/protos \
  --plugin=protoc-gen-ts=/path/to/webpb-protoc-ts \
  --ts_out=path/to/output \
  path/to/your/protos/*.proto
```

## 运行示例

克隆仓库后，分别在两个终端启动后端与前端。

### 后端（端口 8181）

**Gradle：**

```shell
./gradlew sample:backend:bootRun
```

Windows：

```shell
./gradlew.bat sample:backend:bootRun
```

Gradle 会应用 `io.github.jinganix.webpb.java`、调用 `webpb` protoc 插件及注解处理器，在 `sample/backend/build/generated/` 下生成 Java 源码。

**Maven：**

```shell
cd sample/backend
mvn spring-boot:run
```

Maven 通过 `protobuf-maven-plugin` 调用 `webpb` protoc 插件，并以 `webpb-processor` 作为注解处理器；生成代码位于 `sample/backend/target/generated-sources/protobuf/`。

### 前端（端口 4200）

安装 [Node.js](https://nodejs.org/)，构建本地 `webpb` npm 包后启动前端：

```shell
cd runtime/ts && npm ci && npm run build
cd sample/frontend
npm install
npm start
```

`npm start` 会执行 `webpb generate`，自动解析 `protoc` 与 `webpb-protoc-ts`，并将 TypeScript 写入 `sample/frontend/generated/proto/`。详见 [npm / TypeScript](#npm--typescript)。

打开 http://localhost:4200。开发服务器将 `/stores` 代理到后端 http://localhost:8181。

## 参与贡献

参见 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何提交 issue 与贡献代码。
