import {
  AutoAliasRequest,
  AutoAliasResponse,
  ContextPathRequest,
  ContextPathResponse,
  EnumStringRequest,
  EnumStringResponse,
  FieldAliasRequest,
  FieldAliasResponse,
  HttpRouteRequest,
  HttpRouteResponse,
  Int64StringRequest,
  Int64StringResponse,
  QueryParamRequest,
  QueryParamResponse,
  StatusPb,
  ValidationRequest,
  ValidationResponse,
} from "@proto/OptionsProto";
import { WebpbMessage } from "webpb";

export interface OptionField {
  defaultValue: string;
  key: string;
  label: string;
}

export interface ExampleDefinition {
  category?: "enum" | "field" | "file" | "message";
  createRequest: (values: Record<string, string>) => WebpbMessage;
  description: string;
  fields: OptionField[];
  id: string;
  protoSnippet: string;
  responseType?: { prototype: WebpbMessage };
  title: string;
}

export type OptionExample = ExampleDefinition & {
  category: "enum" | "field" | "file" | "message";
};

export const FILE_OPTION_EXAMPLE: ExampleDefinition = {
  category: "file",
  createRequest: () => HttpRouteRequest.create({ name: "file-options" }),
  description:
    "Shared defaults in WebpbOptions.proto apply to every imported proto file.",
  fields: [],
  id: "file-options",
  protoSnippet: `// WebpbOptions.proto
option (f_opts).java = {
  import: 'jakarta.validation.Valid'
  import: 'jakarta.validation.constraints.NotBlank'
};
option (f_opts).ts = {
  int64_as_string: false
};`,
  title: "File options (f_opts)",
};

export const OPTION_EXAMPLES: OptionExample[] = [
  {
    category: "message",
    createRequest: (values) =>
      HttpRouteRequest.create({ name: values.name ?? "webpb" }),
    description:
      "Bind HTTP method and path on request messages via (m_opts).opt.",
    fields: [{ defaultValue: "webpb", key: "name", label: "name" }],
    id: "http-route",
    protoSnippet: `message HttpRouteRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/options/http-route"
  };
  required string name = 1;
}
message HttpRouteResponse {
  required string echo = 1;
}`,
    responseType: HttpRouteResponse,
    title: "HTTP route",
  },
  {
    category: "message",
    createRequest: () => ContextPathRequest.create({}),
    description: "Prefix request paths with (m_opts).opt.context.",
    fields: [],
    id: "context-path",
    protoSnippet: `message ContextPathRequest {
  option (m_opts).opt = {
    method: "GET"
    context: "api"
    path: "/options/context"
  };
}
message ContextPathResponse {
  required string clientPath = 1;
  required string serverPath = 2;
}`,
    responseType: ContextPathResponse,
    title: "Context path",
  },
  {
    category: "field",
    createRequest: (values) =>
      QueryParamRequest.create({ tag: values.tag ?? "demo" }),
    description:
      "Map fields to query string parameters via (opts).opt.in_query.",
    fields: [{ defaultValue: "demo", key: "tag", label: "tag" }],
    id: "in-query",
    protoSnippet: `message QueryParamRequest {
  option (m_opts).opt = {
    method: "GET"
    path: "/options/query?tag={tag}"
  };
  required string tag = 1 [(opts).opt = {in_query: true}];
}
message QueryParamResponse {
  required string tag = 1;
}`,
    responseType: QueryParamResponse,
    title: "Query parameter",
  },
  {
    category: "field",
    createRequest: (values) =>
      ValidationRequest.create({ code: values.code ?? "" }),
    description: "Attach Jakarta validation via (opts).java.annotation.",
    fields: [{ defaultValue: "ok", key: "code", label: "code" }],
    id: "validation",
    protoSnippet: `message ValidationRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/options/validation"
  };
  required string code = 1 [(opts).java = {annotation: '@NotBlank'}];
}
message ValidationResponse {
  required bool ok = 1;
}`,
    responseType: ValidationResponse,
    title: "Validation",
  },
  {
    category: "field",
    createRequest: (values) =>
      Int64StringRequest.create({ id: values.id ?? "9007199254740991" }),
    description: "Serialize int64 as JSON strings via (opts).ts.as_string.",
    fields: [{ defaultValue: "9007199254740991", key: "id", label: "id" }],
    id: "int64-as-string",
    protoSnippet: `message Int64StringRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/options/int64-string"
  };
  required int64 id = 1 [(opts).ts = {as_string: true}];
}
message Int64StringResponse {
  required int64 id = 1 [(opts).ts = {as_string: true}];
}`,
    responseType: Int64StringResponse,
    title: "int64 as string",
  },
  {
    category: "message",
    createRequest: (values) =>
      AutoAliasRequest.create({ text: values.text ?? "hello" }),
    description: "Derive short JSON keys via (m_opts).ts.auto_alias.",
    fields: [{ defaultValue: "hello", key: "text", label: "text" }],
    id: "auto-alias",
    protoSnippet: `message AutoAliasResponse {
  option (m_opts).ts = {auto_alias: true};
  option (m_opts).java = {
    field_annotation: '@JsonProperty("{{_ALIAS_}}")'
  };
  required AliasPayloadPb payload = 1;
}`,
    responseType: AutoAliasResponse,
    title: "Auto alias",
  },
  {
    category: "field",
    createRequest: (values) =>
      FieldAliasRequest.create({ title: values.title ?? "Webpb" }),
    description: "Override JSON property names via (opts).ts.alias.",
    fields: [{ defaultValue: "Webpb", key: "title", label: "title" }],
    id: "field-alias",
    protoSnippet: `message FieldAliasRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/options/field-alias"
  };
  required string title = 1 [(opts).ts = {alias: 't'}];
}
message FieldAliasResponse {
  required string title = 1 [(opts).ts = {alias: 't'}];
}`,
    responseType: FieldAliasResponse,
    title: "Field alias",
  },
  {
    category: "enum",
    createRequest: (values) =>
      EnumStringRequest.create({
        status: values.status === "active" ? StatusPb.ACTIVE : StatusPb.UNKNOWN,
      }),
    description:
      "Use string enum values via (e_opts).opt.string_value and (v_opts).opt.value.",
    fields: [
      {
        defaultValue: "active",
        key: "status",
        label: "status (unknown|active)",
      },
    ],
    id: "enum-string",
    protoSnippet: `enum StatusPb {
  option (e_opts).opt = {string_value: true};
  UNKNOWN = 0 [(v_opts).opt = {value: "unknown"}];
  ACTIVE = 1 [(v_opts).opt = {value: "active"}];
}
message EnumStringRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/options/enum-string"
  };
  required StatusPb status = 1;
}`,
    responseType: EnumStringResponse,
    title: "Enum string value",
  },
];
