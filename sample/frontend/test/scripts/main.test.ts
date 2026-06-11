import {
  StoreListResponse,
  StoreVisitResponse,
  StoreGreetingResponse,
} from "@proto/StoreProto";
import { HttpService } from "@scripts/http.service";
import { Main } from "@scripts/main";

describe("Main", () => {
  const createElement = (id: string): void => {
    const element = document.createElement("input");
    element.setAttribute("id", id);
    document.body.appendChild(element);
  };

  beforeAll(() => {
    createElement("greetingButton");
    createElement("visitStoreButton");
    createElement("getStoresButton");
  });

  it("should call request once when greeting button is clicked", () => {
    // Given
    HttpService.prototype.request = jest
      .fn()
      .mockResolvedValue({ value: "11" });
    new Main();
    const element = document.getElementById("greetingButton") as HTMLElement;

    // When
    element.click();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request once when greeting succeeds", () => {
    // Given
    const res = StoreGreetingResponse.create({ greeting: "Welcome, Tom" });
    HttpService.prototype.request = jest.fn().mockResolvedValue(res);
    const main = new Main();

    // When
    main.greeting();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request once when greeting request fails", () => {
    // Given
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();

    // When
    main.greeting();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request once when greeting customer input exists", () => {
    // Given
    createElement("greeting-customer");
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();

    // When
    main.greeting();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should handle click when visit store button is clicked", () => {
    // Given
    HttpService.prototype.request = jest
      .fn()
      .mockResolvedValue({ value: "11" });
    new Main();
    const element = document.getElementById("visitStoreButton") as HTMLElement;

    // When / Then
    return element.click();
  });

  it("should handle click when get stores button is clicked", () => {
    // Given
    HttpService.prototype.request = jest
      .fn()
      .mockResolvedValue({ value: "11" });
    new Main();
    const element = document.getElementById("getStoresButton") as HTMLElement;

    // When / Then
    return element.click();
  });

  it("should call request once when visit store succeeds", () => {
    // Given
    const res = StoreVisitResponse.create({
      greeting: "Welcome, Tom",
      store: {
        city: "33",
        id: "11",
        name: "22",
      },
    });
    HttpService.prototype.request = jest.fn().mockResolvedValue(res);
    const main = new Main();

    // When
    main.visitStore();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request once when visit store fails", () => {
    // Given
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();

    // When
    main.visitStore();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request once when get stores succeeds", () => {
    // Given
    const res = StoreListResponse.create({
      paging: { page: 1, size: 10, totalCount: 123, totalPage: 13 },
      stores: [],
    });
    HttpService.prototype.request = jest.fn().mockResolvedValue(res);
    const main = new Main();

    // When
    main.getStores();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request once when get stores fails", () => {
    // Given
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();

    // When
    main.getStores();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should call request twice when dom inputs exist and visit store and get stores are called", () => {
    // Given
    createElement("storeId");
    createElement("customer");
    createElement("pageIndex");
    createElement("pageSize");
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();

    // When
    main.visitStore();
    main.getStores();

    // Then
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(2);
  });
});
