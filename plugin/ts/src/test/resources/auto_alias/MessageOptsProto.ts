// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// MessageOpts.proto

import * as Webpb from "webpb";

export interface ILevel3 {
  test1: number;
}

export class Level3 implements ILevel3, Webpb.WebpbMessage {
  test1!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "Level3";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ILevel3) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () =>
      ({
        class: "Level3",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ILevel3): Level3 {
    return new Level3(p);
  }

  static fromAlias(data?: unknown): Level3 {
    const p = Webpb.toAlias(data, {
      "a": "test1",
    }) as Record<string, unknown>;
    return Object.assign(new Level3(), p);
  }

  toWebpbAlias(): unknown {
    return Webpb.toAlias(this, {
      "test1": "a",
    });
  }
}

export interface ILevel2 {
  test1: number;
  test2: ILevel3;
  test3: ILevel3[];
}

export class Level2 implements ILevel2, Webpb.WebpbMessage {
  test1!: number;
  test2!: ILevel3;
  test3!: ILevel3[];
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "Level2";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ILevel2) {
    Webpb.assign(p, this, []);
    p?.test2 && (this.test2 = Level3.create(p.test2));
    p?.test3 && (this.test3 = p.test3.map((x) => Level3.create(x)));
    this.webpbMeta = () =>
      ({
        class: "Level2",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ILevel2): Level2 {
    return new Level2(p);
  }

  static fromAlias(data?: unknown): Level2 {
    const p = data as Record<string, unknown>;
    p?.test2 && (p.test2 = Level3.fromAlias(p.test2));
    p?.test3 && (p.test3 = (p.test3 as Webpb.WebpbMessage[]).map((x) => Level3.fromAlias(x)));
    return Object.assign(new Level2(), p);
  }

  toWebpbAlias(): unknown {
    return Webpb.toAlias(this, {});
  }
}

export interface ILevel1 {
  test1: number;
  test2: ILevel2;
  test3: ILevel2[];
  test4: ILevel3;
  test5: Record<number, ILevel3>;
  test6: Record<string, ILevel3>;
}

export class Level1 implements ILevel1, Webpb.WebpbMessage {
  test1!: number;
  test2!: ILevel2;
  test3!: ILevel2[];
  test4!: ILevel3;
  test5!: Record<number, ILevel3>;
  test6!: Record<string, ILevel3>;
  webpbMeta: () => Webpb.WebpbMeta;

  static CLASS = "Level1";
  static CONTEXT = "";
  static METHOD = "";
  static PATH = "";

  protected constructor(p?: ILevel1) {
    Webpb.assign(p, this, []);
    p?.test2 && (this.test2 = Level2.create(p.test2));
    p?.test3 && (this.test3 = p.test3.map((x) => Level2.create(x)));
    p?.test4 && (this.test4 = Level3.create(p.test4));
    p?.test5 && (this.test5 = Webpb.mapValues(p.test5, (x) => Level3.create(x)));
    p?.test6 && (this.test6 = Webpb.mapValues(p.test6, (x) => Level3.create(x)));
    this.webpbMeta = () =>
      ({
        class: "Level1",
        context: "",
        method: "",
        path: "",
      } as Webpb.WebpbMeta);
  }

  static create(p?: ILevel1): Level1 {
    return new Level1(p);
  }

  static fromAlias(data?: unknown): Level1 {
    const p = Webpb.toAlias(data, {
      "a": "test1",
      "b": "test2",
      "c": "test3",
      "d": "test4",
      "e": "test5",
      "f": "test6",
    }) as Record<string, unknown>;
    p?.test2 && (p.test2 = Level2.fromAlias(p.test2));
    p?.test3 && (p.test3 = (p.test3 as Webpb.WebpbMessage[]).map((x) => Level2.fromAlias(x)));
    p?.test4 && (p.test4 = Level3.fromAlias(p.test4));
    p?.test5 && (p.test5 = Webpb.mapValues(p.test5, (x) => Level3.fromAlias(x)));
    p?.test6 && (p.test6 = Webpb.mapValues(p.test6, (x) => Level3.fromAlias(x)));
    return Object.assign(new Level1(), p);
  }

  toWebpbAlias(): unknown {
    const p = Webpb.toAlias(this, {
      "test1": "a",
      "test2": "b",
      "test3": "c",
      "test4": "d",
      "test5": "e",
      "test6": "f",
    }) as Record<string, unknown>;
    p.b && (p.b = (p.b as Webpb.WebpbMessage).toWebpbAlias());
    p.c && (p.c = (p.c as Webpb.WebpbMessage[]).map((x) => x.toWebpbAlias()));
    p.d && (p.d = (p.d as Webpb.WebpbMessage).toWebpbAlias());
    p.e && (p.e = Webpb.mapValues(p.e, (x) => x.toWebpbAlias()));
    p.f && (p.f = Webpb.mapValues(p.f, (x) => x.toWebpbAlias()));
    return p;
  }
}
