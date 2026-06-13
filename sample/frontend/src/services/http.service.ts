import axios, { Method } from "axios";
import { WebpbMessage } from "webpb";

export interface RequestLog {
  request?: unknown;
  response?: unknown;
  error?: unknown;
  url?: string;
}

export class HttpService {
  constructor(private baseUrl: string) {}

  async request<T extends WebpbMessage>(
    request: WebpbMessage,
    responseType?: {
      prototype: T;
    },
  ): Promise<{ data: T; log: RequestLog }> {
    const meta = request.webpbMeta();
    const url = `${this.baseUrl}${meta.path}`;
    const log: RequestLog = {
      request,
      url,
    };
    try {
      const body = request.toWebpbAlias();
      const res = await axios.request({
        baseURL: this.baseUrl,
        data: body,
        headers: {
          "Content-Type": "application/json; charset=UTF-8",
        },
        method: meta.method as Method,
        url: meta.path,
      });
      log.response = res.data;
      if (responseType) {
        const castType = responseType as unknown as {
          fromAlias: (data: unknown) => T;
        };
        return {
          data: castType.fromAlias(res.data),
          log: {
            ...log,
            response: castType.fromAlias(res.data),
          },
        };
      }
      return { data: res.data, log };
    } catch (error) {
      const { response } = error as { response?: Record<string, unknown> };
      if (response) {
        log.error = {
          data: response.data,
          headers: response.headers,
          status: response.status,
        };
      } else {
        const axiosError = error as { code?: string; message?: string };
        log.error =
          axiosError.code === "ERR_NETWORK"
            ? {
                hint: "Start the backend with ./gradlew sample:backend:bootRun (port 8181).",
                message: "Network error: could not reach the sample backend.",
              }
            : error;
      }
      throw Object.assign(
        new Error(`Failed when request: ${url}`, { cause: error }),
        {
          log,
        },
      );
    }
  }
}
