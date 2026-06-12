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

[`sample/`](sample/) 是一个端到端的商店 API 演示。

| 模块 | 说明 |
|------|------|
| [`sample/proto`](sample/proto) | 共享 `.proto` 与 `WebpbOptions.proto`（全局 Java/TS 默认配置） |
| [`sample/backend`](sample/backend) | Spring Boot 应用；通过 Gradle 从 proto 生成 Java |
| [`sample/frontend`](sample/frontend) | Webpack + TypeScript 应用；通过 `protoc` 从 proto 生成 TS |

### 定义 HTTP 接口

请求消息在 `(m_opts).opt` 中携带路由元数据。路径段使用 `{field}` 或 `{nested.field}` 占位；查询参数字段用 `(opts).opt = {in_query: true}` 标记。

```protobuf
message StoreListRequest {
  option (m_opts).opt = {
    method: "GET"
    path: "/stores?page={pageable.page}&size={pageable.size}"
  };

  required CommonProto.PageablePb pageable = 1 [
    (opts).opt = {in_query: true},
    (opts).java = {annotation: '@Valid'}
  ];
}
```

### Java 控制器

生成的类型实现 `WebpbMessage`。在控制器方法上加 `@WebpbRequestMapping`，注解处理器会根据请求消息的元数据填充 `@RequestMapping`。

```java
@RestController
public class StoreController {

  @WebpbRequestMapping
  public StoreListResponse getStores(@Valid StoreListRequest request) {
    // ...
  }

  @WebpbRequestMapping
  public StoreVisitResponse getStore(@Valid @RequestBody StoreVisitRequest request) {
    // ...
  }
}
```

在 Spring MVC 配置中注册 `WebpbHandlerMethodArgumentResolver` 与 `WebpbRequestBodyAdvice`（见 [`WebMvcConfiguration.java`](sample/backend/src/main/java/io/github/jinganix/webpb/sample/backend/WebMvcConfiguration.java)）。

### TypeScript 客户端

生成的类通过 `webpbMeta()` 暴露 `method` 与 `path`。示例 [`HttpService`](sample/frontend/src/scripts/http.service.ts) 据此发送 JSON 请求：

```typescript
this.httpService.request(
  StoreListRequest.create({ pageable: { page: 1, size: 3 } }),
  StoreListResponse,
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

### 枚举值 — `(v_opts)`

| 分组 | 字段 | 说明 |
|------|------|------|
| `opt` | `value` | 启用 `string_value` 时的字符串表示 |
| `java` | `annotation` | 每个枚举值的 Java 注解 |

## Protoc 插件

webpb 以标准 [protoc](https://grpc.io/docs/protoc-installation/) 插件形式分发：从 stdin 读取 `CodeGeneratorRequest`，将生成文件写入 `--*_out`。无需 Gradle 或其他构建工具。

### 获取插件二进制

**从 [GitHub Releases](https://github.com/jinganix/webpb/releases) 下载**（推荐）：每个 release 附带原生二进制 `webpb-protoc-java-<platform>` 与 `webpb-protoc-ts-<platform>`（如 `darwin-arm64`、`linux-amd64`、`windows-amd64`）。可按需重命名或创建符号链接为 `webpb-protoc-java` / `webpb-protoc-ts`，赋予可执行权限后，将路径传给 `--plugin`。

**从源码构建**（需要 [Go](https://go.dev/)）：

```shell
cd plugin
make build
```

会在 `plugin/bin/` 下生成 `webpb-protoc-java` 与 `webpb-protoc-ts`（Windows 为 `.exe`）。

### `WebpbExtend.proto` 的 import 路径

每个 `.proto` 必须 import webpb 扩展：

```protobuf
import "webpb/WebpbExtend.proto";
```

通过 `-I` 指定包含 `webpb/` 目录的路径。本仓库中为 `lib/proto/src/main/resources`。

### 生成 Java

插件名为 `webpb`（与 [`sample/backend`](sample/backend/build.gradle.kts) 中 Gradle protobuf 插件 id 一致）：

```shell
protoc \
  -I lib/proto/src/main/resources \
  -I path/to/your/protos \
  --plugin=protoc-gen-webpb=/path/to/webpb-protoc-java \
  --webpb_out=path/to/output \
  path/to/your/protos/*.proto
```

生成的 `.java` 按 Java 包结构写入 `--webpb_out`（如 `com/example/Foo.java`）。Spring MVC 接入还需 [`runtime:java`](runtime/java) 库与 [`runtime:processor`](runtime/processor) 注解处理器。

### 生成 TypeScript

插件名为 `ts`：

```shell
protoc \
  -I lib/proto/src/main/resources \
  -I path/to/your/protos \
  --plugin=protoc-gen-ts=/path/to/webpb-protoc-ts \
  --ts_out=path/to/output \
  path/to/your/protos/*.proto
```

每个 protobuf 包生成一个 `.ts` 文件（如 `StoreProto.ts`）。示例前端在 [`sample/frontend/scripts/generate-proto.mjs`](sample/frontend/scripts/generate-proto.mjs) 中使用相同参数。

## 运行示例

克隆仓库后，分别在两个终端启动后端与前端。

### 后端（端口 8181）

```shell
./gradlew sample:backend:bootRun
```

Windows：

```shell
./gradlew.bat sample:backend:bootRun
```

Gradle 会调用 `webpb` protoc 插件（`plugin/bin/webpb-protoc-java`）及注解处理器，在 `sample/backend/build/generated/` 下生成 Java 源码。

### 前端（端口 4200）

安装 [Node.js](https://nodejs.org/) 与 [protoc](https://grpc.io/docs/protoc-installation/) 后：

```shell
cd sample/frontend
npm install
npm start
```

`npm start` 会执行 `scripts/generate-proto.mjs`：在需要时从 `plugin/` 构建 `webpb-protoc-ts`，并将 TypeScript 写入 `sample/frontend/generated/proto/`。等价的 `protoc` 命令见 [Protoc 插件](#protoc-插件)。

打开 http://localhost:4200。开发服务器将 `/stores` 代理到后端 http://localhost:8181。

## 参与贡献

参见 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何提交 issue 与贡献代码。
