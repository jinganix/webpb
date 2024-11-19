import { StoreVisitRequest, StoreVisitResponse } from "@proto/StoreProto";
import { HttpService } from "@scripts/http.service";
import axios from "axios";

jest.mock("axios");

describe("http.service", () => {
  const RESPONSE_DATA = StoreVisitResponse.create({
    greeting: "Welcome, Tom",
    store: {
      city: "33",
      id: "11",
      name: "22",
    },
  });

  it("should request success", async () => {
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockResolvedValue({
      data: RESPONSE_DATA,
      status: 200,
    });
    const res = await httpService.request<StoreVisitResponse>(
      StoreVisitRequest.create({ customer: "Tom", id: "123" }),
    );
    expect(res).toMatchObject(RESPONSE_DATA);
  });

  it("should request success from alias", async () => {
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockResolvedValue({
      data: RESPONSE_DATA,
      status: 200,
    });
    const res = await httpService.request(
      StoreVisitRequest.create({ customer: "Tom", id: "123" }),
      StoreVisitResponse,
    );
    expect(res).toMatchObject(RESPONSE_DATA);
  });

  it("should request failed", async () => {
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockResolvedValue({
      data: RESPONSE_DATA,
      status: 500,
    });
    const res = await httpService.request<StoreVisitResponse>(
      StoreVisitRequest.create({ customer: "Tom", id: "123" }),
    );
    expect(res).toMatchObject(RESPONSE_DATA);
  });

  it("given error with response when request then log success", async () => {
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockRejectedValue({
      response: {
        data: JSON.stringify({ error: "invalid" }),
        status: 500,
      },
    });
    await expect(() =>
      httpService.request<StoreVisitResponse>(
        StoreVisitRequest.create({ customer: "Tom", id: "123" }),
      ),
    ).rejects.toThrowError("Failed when request: https://abc/stores/123");
  });

  it("given error without response when request then log success", async () => {
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockRejectedValue("error");
    await expect(() =>
      httpService.request<StoreVisitResponse>(
        StoreVisitRequest.create({ customer: "Tom", id: "123" }),
      ),
    ).rejects.toThrowError("Failed when request: https://abc/stores/123");
  });
});
