// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Test10.proto

import * as Webpb from "webpb";

export interface ITest10 {
  test1: number;
}

export class Test10 implements ITest10, Webpb.WebpbMessage {
  test1!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "Test10";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ITest10) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "Test10",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ITest10): Test10 {
    return new Test10(p);
  }

  static fromAlias(data?: unknown): ITest10 {
    return Test10.create(data as ITest10);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}
