import { PageablePb } from "@proto/CommonProto";
import {
  StoreGreetingRequest,
  StoreGreetingResponse,
  StoreListRequest,
  StoreListResponse,
  StoreVisitRequest,
  StoreVisitResponse,
} from "@proto/StoreProto";
import { WebpbMessage } from "webpb";

import { type ExampleDefinition } from "@/options/option-definitions";

function toPositiveInt(value: string | undefined, fallback: number): number {
  const parsed = Number.parseInt(value ?? "", 10);
  return Number.isNaN(parsed) ? fallback : parsed;
}

function greetingRequest(values: Record<string, string>): WebpbMessage {
  return StoreGreetingRequest.create({ customer: values.customer ?? "webpb" });
}

function listRequest(values: Record<string, string>): WebpbMessage {
  return StoreListRequest.create({
    pageable: PageablePb.create({
      page: toPositiveInt(values.page, 1),
      size: toPositiveInt(values.size, 2),
    }),
  });
}

function visitRequest(values: Record<string, string>): WebpbMessage {
  return StoreVisitRequest.create({
    customer: values.customer ?? "webpb",
    id: values.id ?? "123",
  });
}

export const STORE_EXAMPLES: ExampleDefinition[] = [
  {
    createRequest: greetingRequest,
    description:
      "Greet a customer through the Store API. Uses HttpService with the Vite proxy.",
    fields: [{ defaultValue: "webpb", key: "customer", label: "customer" }],
    id: "store-greeting",
    protoSnippet: `message StoreGreetingRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/stores/greeting"
  };
  required string customer = 1;
}
message StoreGreetingResponse {
  required string greeting = 1;
}`,
    responseType: StoreGreetingResponse,
    title: "Store greeting",
  },
  {
    createRequest: listRequest,
    description:
      "List stores with paging through the Store API. Uses HttpService with the Vite proxy.",
    fields: [
      { defaultValue: "1", key: "page", label: "page" },
      { defaultValue: "2", key: "size", label: "size" },
    ],
    id: "store-list",
    protoSnippet: `message StoreListRequest {
  option (m_opts).opt = {
    method: "GET"
    path: "/stores?page={pageable.page}&size={pageable.size}"
  };
  required CommonProto.PageablePb pageable = 1;
}`,
    responseType: StoreListResponse,
    title: "Store list",
  },
  {
    createRequest: visitRequest,
    description:
      "Visit a store by path id through the Store API. Uses HttpService with the Vite proxy.",
    fields: [
      { defaultValue: "123", key: "id", label: "id" },
      { defaultValue: "webpb", key: "customer", label: "customer" },
    ],
    id: "store-visit",
    protoSnippet: `message StoreVisitRequest {
  option (m_opts).opt = {
    method: "POST"
    path: "/stores/{id}"
  };
  required int64 id = 1 [(opts).ts = {as_string: true}];
  required string customer = 2;
}`,
    responseType: StoreVisitResponse,
    title: "Store visit",
  },
];
