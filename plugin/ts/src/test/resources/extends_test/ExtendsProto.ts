// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// include/Extends.proto

import * as Webpb from "webpb";

export interface IExtends {
  foo_1: number;
  bar_1: string;
}

export class Extends implements IExtends, Webpb.WebpbMessage {
  foo_1!: number;
  bar_1!: string;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "Extends";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: IExtends) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "Extends",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: IExtends): Extends {
    return new Extends(p);
  }

  static fromAlias(data?: unknown): IExtends {
    return Extends.create(data as IExtends);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}
