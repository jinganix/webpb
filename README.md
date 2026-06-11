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

Gradle runs the `webpb` protoc plugin (`plugin/bin/webpb-protoc-java`) and the annotation processor to generate Java sources under `sample/backend/build/generated/`.

### Frontend (port 4200)

Install [Node.js](https://nodejs.org/) and [protoc](https://grpc.io/docs/protoc-installation/), then:

```shell
cd sample/frontend
npm install
npm start
```

`npm start` runs `scripts/generate-proto.mjs`, which builds `webpb-protoc-ts` from `plugin/` if needed and writes TypeScript to `sample/frontend/generated/proto/`.

Open http://localhost:4200. The dev server proxies `/stores` to the backend at http://localhost:8181.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to report issues and submit changes.
