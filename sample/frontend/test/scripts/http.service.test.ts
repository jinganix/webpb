import { StoreVisitRequest, StoreVisitResponse } from "@proto/StoreProto";
import { HttpService } from "@scripts/http.service";
import axios from "axios";

jest.mock("axios");

describe("HttpService", () => {
  const RESPONSE_DATA = StoreVisitResponse.create({
    greeting: "Welcome, Tom",
    store: {
      city: "33",
      id: "11",
      name: "22",
    },
  });

  it("should return response data when request succeeds", async () => {
    // Given
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockResolvedValue({
      data: RESPONSE_DATA,
      status: 200,
    });
    const request = StoreVisitRequest.create({ customer: "Tom", id: "123" });

    // When
    const res = await httpService.request<StoreVisitResponse>(request);

    // Then
    expect(res).toMatchObject(RESPONSE_DATA);
  });

  it("should return response data when request succeeds with response type", async () => {
    // Given
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockResolvedValue({
      data: RESPONSE_DATA,
      status: 200,
    });
    const request = StoreVisitRequest.create({ customer: "Tom", id: "123" });

    // When
    const res = await httpService.request(request, StoreVisitResponse);

    // Then
    expect(res).toMatchObject(RESPONSE_DATA);
  });

  it("should return response data when status is not 200", async () => {
    // Given
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockResolvedValue({
      data: RESPONSE_DATA,
      status: 500,
    });
    const request = StoreVisitRequest.create({ customer: "Tom", id: "123" });

    // When
    const res = await httpService.request<StoreVisitResponse>(request);

    // Then
    expect(res).toMatchObject(RESPONSE_DATA);
  });

  it("should throw when axios rejects with response", async () => {
    // Given
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockRejectedValue({
      response: {
        data: JSON.stringify({ error: "invalid" }),
        status: 500,
      },
    });
    const request = StoreVisitRequest.create({ customer: "Tom", id: "123" });

    // When / Then
    await expect(() => httpService.request<StoreVisitResponse>(request)).rejects.toThrow(
      "Failed when request: https://abc/stores/123",
    );
  });

  it("should throw when axios rejects without response", async () => {
    // Given
    const httpService = new HttpService("https://abc");
    axios.request = jest.fn().mockRejectedValue("error");
    const request = StoreVisitRequest.create({ customer: "Tom", id: "123" });

    // When / Then
    await expect(() => httpService.request<StoreVisitResponse>(request)).rejects.toThrow(
      "Failed when request: https://abc/stores/123",
    );
  });
});
