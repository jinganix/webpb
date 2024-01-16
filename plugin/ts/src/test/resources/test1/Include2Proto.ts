// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Include2.proto

import * as Webpb from "webpb";

export interface IMessage {
  id: number;
}

export class Message implements IMessage, Webpb.WebpbMessage {
  id!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "Message";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: IMessage) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "Message",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: IMessage): Message {
    return new Message(p);
  }

  static fromAlias(data?: unknown): IMessage {
    return Message.create(data as IMessage);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export namespace Message {
  export interface INested {
    test1: number;
  }

  export class Nested implements INested, Webpb.WebpbMessage {
    test1!: number;
    webpbMeta: () => Webpb.WebpbMeta;

    static CLASS = "Nested";
    static CONTEXT = "";
    static METHOD = "";
    static PATH = "";

    protected constructor(p?: INested) {
      Webpb.assign(p, this, []);
      this.webpbMeta = () =>
        ({
          class: "Nested",
          context: "",
          method: "",
          path: "",
        } as Webpb.WebpbMeta);
    }

    static create(p?: INested): Nested {
      return new Nested(p);
    }

    static fromAlias(data?: unknown): INested {
      return Nested.create(data as INested);
    }

    toWebpbAlias(): unknown {
      return this;
    }
  }
}
