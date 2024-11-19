import axios, { Method } from "axios";
import { WebpbMessage } from "webpb";
import { logger } from "./logger";

export class HttpService {
  constructor(private baseUrl: string) {}

  async request<T extends WebpbMessage>(
    request: WebpbMessage,
    responseType?: {
      prototype: T;
    },
  ): Promise<T> {
    logger.reset();
    const meta = request.webpbMeta();
    const url = `${this.baseUrl}${meta.path}`;
    logger.log(`====> Request (${url}):`);
    logger.stringify(request);
    try {
      const res = await axios.request({
        baseURL: this.baseUrl,
        data: JSON.stringify(request),
        headers: {
          "Content-Type": "application/json; charset=UTF-8",
        },
        method: meta.method as Method,
        url: meta.path,
      });
      console.log(res);
      logger.log("\n====> Raw Response:");
      logger.stringify(res.data);

      if (responseType) {
        const castType = responseType as unknown as {
          fromAlias: (data: unknown) => T;
        };
        logger.log("\n====> Response:");
        logger.stringify(castType.fromAlias(res.data));
      }
      return res.data;
    } catch (error) {
      logger.log("\n====> Error:");
      const { response } = error as { response: Record<string, unknown> };
      if (response) {
        logger.stringify({
          data: response.data,
          headers: response.headers,
          status: response.status,
        });
      } else {
        logger.stringify(error);
      }
      throw new Error(`Failed when request: ${url}`);
    }
  }
}
