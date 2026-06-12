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

[中文版本](README.zh.md)

# webpb

Generate web API models and routing metadata from Protocol Buffers.

A single `.proto` file describes request/response types, HTTP method, path templates, validation annotations, and JSON field naming. The protoc plugins emit:

- **Java** — POJO classes (`WebpbMessage`), Jackson/validation annotations, and Spring `@WebpbRequestMapping` wiring via an annotation processor.
- **TypeScript** — classes with `webpbMeta()`, `create()`, and `fromAlias()` for typed HTTP clients.

Proto extensions are defined in [`lib/proto/src/main/resources/webpb/WebpbExtend.proto`](lib/proto/src/main/resources/webpb/WebpbExtend.proto).

## Sample project

[`sample/`](sample/) is an end-to-end store API demo.

| Module | Role |
|--------|------|
| [`sample/proto`](sample/proto) | Shared `.proto` files and `WebpbOptions.proto` (global Java/TS defaults) |
| [`sample/backend`](sample/backend) | Spring Boot app; generates Java from proto via Gradle |
| [`sample/maven`](sample/maven) | Minimal Maven example with `protobuf-maven-plugin` |
| [`sample/frontend`](sample/frontend) | Webpack + TypeScript app; generates TS from proto via `protoc` |

### Define an HTTP endpoint

Request messages carry routing metadata in `(m_opts).opt`. Path segments use `{field}` or `{nested.field}` placeholders; mark query fields with `(opts).opt = {in_query: true}`.

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

### Java controller

Generated types implement `WebpbMessage`. Add `@WebpbRequestMapping` on controller methods; the annotation processor fills in `@RequestMapping` from the request message metadata.

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

Register `WebpbHandlerMethodArgumentResolver` and `WebpbRequestBodyAdvice` in Spring MVC configuration (see [`WebMvcConfiguration.java`](sample/backend/src/main/java/io/github/jinganix/webpb/sample/backend/WebMvcConfiguration.java)).

### TypeScript client

Generated classes expose `webpbMeta()` with `method` and `path`. The sample [`HttpService`](sample/frontend/src/scripts/http.service.ts) sends JSON requests using that metadata:

```typescript
this.httpService.request(
  StoreListRequest.create({ pageable: { page: 1, size: 3 } }),
  StoreListResponse,
);
```

### Global options (`WebpbOptions.proto`)

Per-project defaults live in a shared `WebpbOptions.proto` imported by every file. The sample declares Java imports for validation annotations and TS defaults:

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

Field-level Jackson naming in the sample uses message options and placeholders:

```protobuf
option (m_opts).java = {field_annotation: '@JsonProperty("{{_ALIAS_}}")'};
option (m_opts).ts = {auto_alias: true};
```

## Proto options reference

Options are attached at file, message, enum, field, or enum-value level. Import `webpb/WebpbExtend.proto` in every proto file.

### File — `(f_opts)`

| Group | Field | Description |
|-------|-------|-------------|
| `java` | `gen_getter` | Generate getters (default `true`) |
| `java` | `gen_setter` | Generate setters (default `true`) |
| `java` | `import` | Extra Java imports for generated annotations/types |
| `java` | `annotation` | Class-level annotations on every generated type |
| `java` | `field_annotation` | Field annotations; supports `{{_ALIAS_}}`, `{{_FIELD_NAME_}}` |
| `java` | `repeatable_annotation` | Fully qualified annotation types allowed to repeat |
| `ts` | `import` | Extra TypeScript imports |
| `ts` | `int64_as_string` | Serialize `int64` as string in JSON |
| `ts` | `auto_alias` | Derive JSON field names from proto field names |
| `ts` | `default_const_enum` | Emit const enums for protobuf enums |

### Message — `(m_opts)`

| Group | Field | Description |
|-------|-------|-------------|
| `opt` | `method` | HTTP method for request messages (e.g. `GET`, `POST`) |
| `opt` | `path` | URL template with `{field}` placeholders |
| `opt` | `context` | Base path prefix |
| `opt` | `extends` | Java superclass name |
| `opt` | `implements` | Java interfaces to implement |
| `opt` | `sub_type` | Subtype discriminator for polymorphic messages |
| `opt` | `sub_values` | Allowed subtype values |
| `java` | `annotation` | Class-level Java annotations |
| `java` | `field_annotation` | Default field annotations for all fields in the message |
| `ts` | `auto_alias` | Override file-level `auto_alias` for this message |
| `ts` | `alias_reserve` | Reserved alias index for child messages; must be greater than this message's max field id. Child alias index is `max(ancestor max index, alias_reserve) + field id` |

### Field — `(opts)`

| Group | Field | Description |
|-------|-------|-------------|
| `opt` | `omitted` | Exclude field from generated API surface |
| `opt` | `in_query` | Bind field to query string (for GET / path templates) |
| `java` | `annotation` | Java field annotations (e.g. `@NotNull`, `@Pattern(...)`) |
| `java` | `as_set` | Generate repeated fields as `Set<T>` instead of `List<T>` |
| `java` | `as_collection` | Generate repeated fields as `Collection<T>` instead of `List<T>` |
| `ts` | `as_string` | Serialize numeric field as string |
| `ts` | `alias` | JSON property name override |
| `ts` | `auto_alias` | Override alias behavior for this field |

Repeated fields default to `List<T>`. Use Java field options to change the collection type:

```protobuf
repeated string tags = 1 [(opts).java = {as_set: true}];
repeated int32 ids = 2 [(opts).java = {as_collection: true}];
```

### Enum — `(e_opts)`

| Group | Field | Description |
|-------|-------|-------------|
| `opt` | `string_value` | Use string values instead of numeric ordinals |
| `java` | `annotation` | Enum-level Java annotations |
| `java` | `implements` | Java interfaces for the enum |
| `ts` | `default_const_enum` | Override file-level const-enum behavior |

### Enum value — `(v_opts)`

| Group | Field | Description |
|-------|-------|-------------|
| `opt` | `value` | String representation when `string_value` is enabled |
| `java` | `annotation` | Per-value Java annotations |

## Getting started

Published artifacts (Maven Central, group `io.github.jinganix.webpb`):

| Artifact | Purpose |
|----------|---------|
| `webpb-gradle-plugin` | Gradle convention plugins |
| `webpb-protoc-java` | Java protoc plugin (`:all` fat jar or platform binary) |
| `webpb-protoc-ts` | TypeScript protoc plugin (`:all` fat jar or platform binary) |
| `webpb-proto` | `WebpbExtend.proto` and well-known types for `-I` / `protobuf(...)` deps |
| `webpb-runtime` | Java runtime library |
| `webpb-processor` | Spring `@WebpbRequestMapping` annotation processor |
| [`webpb` (npm)](https://www.npmjs.com/package/webpb) | TypeScript runtime and `webpb generate` CLI |

Every `.proto` file must import webpb extensions:

```protobuf
import "webpb/WebpbExtend.proto";
```

Add `webpb-proto` as a dependency (Gradle/Maven) or pass `-I` to a directory that contains the `webpb/` folder.

### Gradle

Apply the [protobuf Gradle plugin](https://github.com/google/protobuf-gradle-plugin) together with a webpb convention plugin:

**Java** ([`sample/backend`](sample/backend/build.gradle.kts)):

```kotlin
plugins {
  id("com.google.protobuf") version "0.9.6"
  id("io.github.jinganix.webpb.java") version "0.0.27"
}

dependencies {
  protobuf("io.github.jinganix.webpb:webpb-proto:0.0.27")
  implementation("io.github.jinganix.webpb:webpb-runtime:0.0.27")
  annotationProcessor("io.github.jinganix.webpb:webpb-processor:0.0.27")
  // your .proto module or files
  protobuf(project(":your-proto-module"))
}
```

**TypeScript**:

```kotlin
plugins {
  id("com.google.protobuf") version "0.9.6"
  id("io.github.jinganix.webpb.ts") version "0.0.27"
}

dependencies {
  protobuf("io.github.jinganix.webpb:webpb-proto:0.0.27")
}
```

Optional configuration:

```kotlin
webpb {
  webpbVersion = "0.0.27"      // defaults to the Gradle plugin version
  protobufVersion = "4.35.0"   // com.google.protobuf:protoc version
  cleanOutput = true           // delete output dir before generation
  localPluginPath = "/path/to/webpb-protoc-java" // skip Maven resolution
}
```

The convention plugin configures `protoc`, registers the `webpb` or `ts` plugin from Maven (`webpb-protoc-*:all@jar`), removes the built-in `java` generator, and wires `generateProto` tasks. When developing webpb from source, a binary under `plugin/bin/` is picked up automatically.

Manual setup (without the convention plugin) is also supported:

```kotlin
protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:4.35.0"
  }
  plugins {
    id("ts") {
      artifact = "io.github.jinganix.webpb:webpb-protoc-ts:0.0.27:all@jar"
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

Use [protobuf-maven-plugin](https://www.xolstice.org/protobuf-maven-plugin/) with the published `webpb-protoc-java` launcher JAR (`classifier: all`). See [`sample/maven/pom.xml`](sample/maven/pom.xml).

```xml
<properties>
  <webpb.version>0.0.27</webpb.version>
  <protobuf.version>4.35.0</protobuf.version>
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

Run `mvn generate-sources` to generate Java under `target/generated-sources/protobuf/`.

For Spring MVC, add `webpb-processor` as an annotation processor dependency.

### npm / TypeScript

Install the runtime (dev dependency) and generate TypeScript with the bundled CLI:

```shell
npm install -D webpb
```

The `webpb` CLI resolves `protoc` and `webpb-protoc-ts` automatically, includes bundled `webpb/WebpbExtend.proto`, and writes one `.ts` file per protobuf package (for example `StoreProto.ts`).

```shell
npx webpb generate \
  -o src/generated \
  -I path/to/your/protos \
  path/to/your/protos/*.proto
```

Common options:

| Option | Description |
|--------|-------------|
| `-o, --out` | Output directory (`--ts_out`), required |
| `-I, --include` | Additional proto include path (repeatable) |
| `--no-webpb-proto` | Skip bundled webpb proto includes |
| `--protoc` | Path to `protoc` (default: `PATH`, then Maven Central download) |
| `--plugin` | Path to `webpb-protoc-ts` (default: GitHub Releases download) |
| `--webpb-version` | webpb release version for plugin download |
| `--protobuf-version` | `protoc` version for auto-download (default: bundled, aligned with Gradle) |

Environment variables: `PROTOC`, `WEBPB_PROTOC_TS`, `WEBPB_VERSION`, `WEBPB_PROTOBUF_VERSION`.

Resolution order for tools:

**`webpb-protoc-ts`:** `WEBPB_PROTOC_TS` → monorepo `plugin/bin/` (when developing webpb) → cached GitHub release (`~/.cache/webpb/{version}/`) → download.

**`protoc`:** `PROTOC` → `PATH` → cached Maven Central download (`~/.cache/webpb/protoc/{version}/`).

Bundled proto includes ship in the npm package (`node_modules/webpb/proto`). Override with `--no-webpb-proto` when supplying your own `-I` paths.

Add to `package.json`:

```json
{
  "scripts": {
    "proto": "webpb generate -o src/generated -I protos protos/*.proto"
  }
}
```

Sample frontend (uses the local `webpb` package from `runtime/ts/build`; run `npm run build` in `runtime/ts` first when developing from source):

```shell
cd sample/frontend
npm install
npm run proto
```

For manual `protoc` invocation, download a release binary from [GitHub Releases](https://github.com/jinganix/webpb/releases) (`webpb-protoc-ts-<platform>`) or set `WEBPB_PROTOC_TS`. See [protoc CLI](#protoc-cli-any-build-tool) below.

### protoc CLI (any build tool)

webpb plugins are standard [protoc](https://grpc.io/docs/protoc-installation/) plugins. They read `CodeGeneratorRequest` from stdin and write generated files to `--*_out`.

**Get a plugin binary**

- [GitHub Releases](https://github.com/jinganix/webpb/releases) — `webpb-protoc-java-<platform>` / `webpb-protoc-ts-<platform>`
- Maven Central — `io.github.jinganix.webpb:webpb-protoc-java:VERSION:all` (run via `java -jar` or let Gradle/Maven resolve it)
- Build from source (requires [Go](https://go.dev/)): `cd plugin && make build`

**Generate Java** (plugin name `webpb`):

```shell
protoc \
  -I path/to/webpb-proto-include \
  -I path/to/your/protos \
  --plugin=protoc-gen-webpb=/path/to/webpb-protoc-java \
  --webpb_out=path/to/output \
  path/to/your/protos/*.proto
```

**Generate TypeScript** (plugin name `ts`):

```shell
protoc \
  -I path/to/webpb-proto-include \
  -I path/to/your/protos \
  --plugin=protoc-gen-ts=/path/to/webpb-protoc-ts \
  --ts_out=path/to/output \
  path/to/your/protos/*.proto
```

## Run the sample

Clone the repository, then start backend and frontend in separate terminals.

### Backend (port 8181)

```shell
./gradlew sample:backend:bootRun
```

On Windows:

```shell
./gradlew.bat sample:backend:bootRun
```

Gradle applies `io.github.jinganix.webpb.java`, runs the `webpb` protoc plugin, and uses the annotation processor to generate Java sources under `sample/backend/build/generated/`.

### Frontend (port 4200)

Install [Node.js](https://nodejs.org/), build the local `webpb` npm package, then start the frontend:

```shell
cd runtime/ts && npm ci && npm run build
cd sample/frontend
npm install
npm start
```

`npm start` runs `webpb generate`, which resolves `protoc` and `webpb-protoc-ts` and writes TypeScript to `sample/frontend/generated/proto/`. See [npm / TypeScript](#npm--typescript) for details.

Open http://localhost:4200. The dev server proxies `/stores` to the backend at http://localhost:8181.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to report issues and submit changes.
