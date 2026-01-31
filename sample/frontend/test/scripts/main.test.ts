import {
  StoreListResponse,
  StoreVisitResponse,
  StoreGreetingResponse,
} from "@proto/StoreProto";
import { HttpService } from "@scripts/http.service";
import { Main } from "@scripts/main";

describe("main", () => {
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

  describe("when click greetingButton", () => {
    it("then greeting", () => {
      HttpService.prototype.request = jest
        .fn()
        .mockResolvedValue({ value: "11" });
      new Main();
      const element = document.getElementById("greetingButton") as HTMLElement;
      element.click();
      expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
    });

    describe("when request success", () =>
      it("then greeting success", () => {
        const res = StoreGreetingResponse.create({ greeting: "Welcome, Tom" });
        HttpService.prototype.request = jest.fn().mockResolvedValue(res);
        const main = new Main();
        main.greeting();
        expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
      }));

    describe("when request failed", () =>
      it("then greeting failed", () => {
        HttpService.prototype.request = jest
          .fn()
          .mockRejectedValue({ error: "ERROR" });
        const main = new Main();
        main.greeting();
        expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
      }));

    describe("when input customer", () =>
      it("then greeting success", () => {
        createElement("greeting-customer");

        HttpService.prototype.request = jest
          .fn()
          .mockRejectedValue({ error: "ERROR" });
        const main = new Main();
        main.greeting();
        expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
      }));
  });

  it("when click visitStoreButton then get store success", () => {
    HttpService.prototype.request = jest
      .fn()
      .mockResolvedValue({ value: "11" });
    new Main();
    const element = document.getElementById("visitStoreButton") as HTMLElement;
    return element.click();
  });

  it("when click getStoresButton then get store list success", () => {
    HttpService.prototype.request = jest
      .fn()
      .mockResolvedValue({ value: "11" });
    new Main();
    const element = document.getElementById("getStoresButton") as HTMLElement;
    return element.click();
  });

  it("should get store success", () => {
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
    main.visitStore();
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should get store failed", () => {
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();
    main.visitStore();
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should get stores success", () => {
    const res = StoreListResponse.create({
      paging: { page: 1, size: 10, totalCount: 123, totalPage: 13 },
      stores: [],
    });
    HttpService.prototype.request = jest.fn().mockResolvedValue(res);
    const main = new Main();
    main.getStores();
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("should get store failed", () => {
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();
    main.getStores();
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(1);
  });

  it("given dom inputs when get store then return store", () => {
    createElement("storeId");
    createElement("customer");
    createElement("pageIndex");
    createElement("pageSize");
    HttpService.prototype.request = jest
      .fn()
      .mockRejectedValue({ error: "ERROR" });
    const main = new Main();
    main.visitStore();
    main.getStores();
    expect(HttpService.prototype.request).toHaveBeenCalledTimes(2);
  });
});
