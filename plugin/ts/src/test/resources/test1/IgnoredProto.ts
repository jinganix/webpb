// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Ignored.proto

import * as Webpb from "webpb";

export interface IIgnoreTest {
  test1: number;
}

export class IgnoreTest implements IIgnoreTest, Webpb.WebpbMessage {
  test1!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "IgnoreTest";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: IIgnoreTest) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "IgnoreTest",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: IIgnoreTest): IgnoreTest {
    return new IgnoreTest(p);
  }

  static fromAlias(data?: unknown): IgnoreTest {
    return IgnoreTest.create(data as IIgnoreTest);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}
