// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// BadAnnotation.proto

import * as Webpb from "webpb";

export interface IBadAnnotation {
  foo_2: number;
  bar_2: string;
}

export class BadAnnotation implements IBadAnnotation, Webpb.WebpbMessage {
  foo_2!: number;
  bar_2!: string;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "BadAnnotation";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: IBadAnnotation) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "BadAnnotation",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: IBadAnnotation): BadAnnotation {
    return new BadAnnotation(p);
  }

  static fromAlias(data?: unknown): BadAnnotation {
    return BadAnnotation.create(data as IBadAnnotation);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}
