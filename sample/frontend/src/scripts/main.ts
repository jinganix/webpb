import {
  StoreGreetingRequest,
  StoreListRequest,
  StoreListResponse,
  StoreVisitRequest,
  StoreVisitResponse,
} from "@proto/StoreProto";
import { HttpService } from "./http.service";

export class Main {
  private httpService = new HttpService("http://127.0.0.1:4200");

  constructor() {
    Main.addClickListener("greetingButton", () => this.greeting());
    Main.addClickListener("visitStoreButton", () => this.visitStore());
    Main.addClickListener("getStoresButton", () => this.getStores());
  }

  private static addClickListener(id: string, listener: () => void): void {
    const element = document.getElementById(id);
    element && element.addEventListener("click", listener);
  }

  greeting(): void {
    const customerElement = Main.getInput("greeting-customer");
    const customer = customerElement?.value ?? "Tom";
    this.httpService
      .request<StoreVisitResponse>(
        StoreGreetingRequest.create({ customer: customer }),
      )
      .then(
        (res) => console.log(res),
        (error) => console.log(error),
      );
  }

  visitStore(): void {
    const storeIdElement = Main.getInput("storeId");
    const storeId = storeIdElement?.value ?? "12345";
    const customerElement = Main.getInput("customer");
    const customer = customerElement?.value ?? "Tom";
    this.httpService
      .request(
        StoreVisitRequest.create({ customer: customer, id: storeId }),
        StoreVisitResponse,
      )
      .then(
        (res) => console.log(res),
        (error) => console.log(error),
      );
  }

  getStores(): void {
    const indexElement = Main.getInput("pageIndex");
    const pageIndex = Number(indexElement?.value ?? "1");
    const sizeElement = Main.getInput("pageSize");
    const pageSize = Number(sizeElement?.value ?? "3");
    this.httpService
      .request<StoreListResponse>(
        StoreListRequest.create({
          pageable: { page: pageIndex, size: pageSize },
        }),
      )
      .then(
        (res) => console.log(res),
        (error) => console.log(error),
      );
  }

  private static getInput(id: string): HTMLInputElement {
    return document.getElementById(id) as HTMLInputElement;
  }
}

new Main();
