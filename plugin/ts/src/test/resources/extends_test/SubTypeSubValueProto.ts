// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// SubTypeSubValue.proto

import * as Webpb from "webpb";
import * as AnotherEnum from "./AnotherEnum";
import * as SubTypeSubValueProto from "./SubTypeSubValueProto";
import("./SubTypeSubValueSuperFromAlias");

export enum SubTypeSubValueType {
  subTypeSubValue0 = 0,
  subTypeSubValue1 = 1,
  subTypeSubValue2 = 2,
}

export const SubTypeSubValueTypeValues = [
  SubTypeSubValueType.subTypeSubValue0,
  SubTypeSubValueType.subTypeSubValue1,
  SubTypeSubValueType.subTypeSubValue2,
];

export const enum ConstSubTypeSubValueType {
  subTypeSubValue0 = 0,
  subTypeSubValue1 = 1,
  subTypeSubValue2 = 2,
}

export const ConstSubTypeSubValueTypeValues = [
  ConstSubTypeSubValueType.subTypeSubValue0,
  ConstSubTypeSubValueType.subTypeSubValue1,
  ConstSubTypeSubValueType.subTypeSubValue2,
];

export interface ISubTypeSubValueStringSuper {
  type: string;
}

export class SubTypeSubValueStringSuper implements ISubTypeSubValueStringSuper, Webpb.WebpbMessage {
  type!: string;
  webpbMeta: () => Webpb.WebpbMeta;
  static fromAliases: Record<string, (data?: unknown) => SubTypeSubValueStringSuper> = {};

  static CLASS = "SubTypeSubValueStringSuper";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValueStringSuper) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValueStringSuper",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValueStringSuper): SubTypeSubValueStringSuper {
    return new SubTypeSubValueStringSuper(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValueStringSuper {
    return SubTypeSubValueStringSuper.create(data as ISubTypeSubValueStringSuper);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValueSuper<T extends SubTypeSubValueType = SubTypeSubValueType> {
  type: T;
}

export class SubTypeSubValueSuper<T extends SubTypeSubValueType = SubTypeSubValueType> implements ISubTypeSubValueSuper<T>, Webpb.WebpbMessage {
  type!: T;
  webpbMeta: () => Webpb.WebpbMeta;
  static fromAliases: Record<string, (data?: unknown) => SubTypeSubValueSuper> = {};

  static CLASS = "SubTypeSubValueSuper";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValueSuper) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValueSuper",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValueSuper): SubTypeSubValueSuper {
    return new SubTypeSubValueSuper(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValueSuper {
    return SubTypeSubValueSuper.create(data as ISubTypeSubValueSuper);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue0 extends SubTypeSubValueProto.ISubTypeSubValueSuper {
  value: number;
}

export class SubTypeSubValue0 extends SubTypeSubValueProto.SubTypeSubValueSuper implements ISubTypeSubValue0, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue0";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue0) {
    super();
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue0",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue0): SubTypeSubValue0 {
    return new SubTypeSubValue0(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue0 {
    return SubTypeSubValue0.create(data as ISubTypeSubValue0);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue1 extends SubTypeSubValueProto.ISubTypeSubValueSuper<SubTypeSubValueType.subTypeSubValue0> {
  value: number;
}

export class SubTypeSubValue1 extends SubTypeSubValueProto.SubTypeSubValueSuper<SubTypeSubValueType.subTypeSubValue0> implements ISubTypeSubValue1, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue1";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue1) {
    super();
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue1",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue1): SubTypeSubValue1 {
    return new SubTypeSubValue1(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue1 {
    return SubTypeSubValue1.create(data as ISubTypeSubValue1);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue2 extends SubTypeSubValueProto.ISubTypeSubValueSuper<SubTypeSubValueType.subTypeSubValue1 | SubTypeSubValueType.subTypeSubValue2> {
  value: number;
}

export class SubTypeSubValue2 extends SubTypeSubValueProto.SubTypeSubValueSuper<SubTypeSubValueType.subTypeSubValue1 | SubTypeSubValueType.subTypeSubValue2> implements ISubTypeSubValue2, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue2";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue2) {
    super();
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue2",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue2): SubTypeSubValue2 {
    return new SubTypeSubValue2(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue2 {
    return SubTypeSubValue2.create(data as ISubTypeSubValue2);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue3 {
  value: number;
}

export class SubTypeSubValue3 implements ISubTypeSubValue3, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue3";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue3) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue3",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue3): SubTypeSubValue3 {
    return new SubTypeSubValue3(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue3 {
    return SubTypeSubValue3.create(data as ISubTypeSubValue3);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue4 extends SubTypeSubValueProto.ISubTypeSubValueSuper<"foo"> {
  value: number;
}

export class SubTypeSubValue4 extends SubTypeSubValueProto.SubTypeSubValueSuper<"foo"> implements ISubTypeSubValue4, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue4";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue4) {
    super();
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue4",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue4): SubTypeSubValue4 {
    return new SubTypeSubValue4(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue4 {
    return SubTypeSubValue4.create(data as ISubTypeSubValue4);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue5 extends SubTypeSubValueProto.ISubTypeSubValueSuper<"foo.bar"> {
  value: number;
}

export class SubTypeSubValue5 extends SubTypeSubValueProto.SubTypeSubValueSuper<"foo.bar"> implements ISubTypeSubValue5, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue5";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue5) {
    super();
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue5",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue5): SubTypeSubValue5 {
    return new SubTypeSubValue5(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue5 {
    return SubTypeSubValue5.create(data as ISubTypeSubValue5);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}

export interface ISubTypeSubValue6 extends SubTypeSubValueProto.ISubTypeSubValueSuper<AnotherEnum.Another.a> {
  value: number;
}

export class SubTypeSubValue6 extends SubTypeSubValueProto.SubTypeSubValueSuper<AnotherEnum.Another.a> implements ISubTypeSubValue6, Webpb.WebpbMessage {
  value!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "SubTypeSubValue6";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ISubTypeSubValue6) {
    super();
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "SubTypeSubValue6",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ISubTypeSubValue6): SubTypeSubValue6 {
    return new SubTypeSubValue6(p);
  }

  static fromAlias(data?: unknown): SubTypeSubValue6 {
    return SubTypeSubValue6.create(data as ISubTypeSubValue6);
  }

  toWebpbAlias(): unknown {
    return this;
  }
}
