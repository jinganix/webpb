// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// FieldOpts.proto

import * as Webpb from "webpb";

export interface ILevel3 {
  test1: number;
}

export class Level3 implements ILevel3, Webpb.WebpbMessage {
  test1!: number;
  webpbMeta: () => Webpb.WebpbMeta;

  protected constructor(p?: ILevel3) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () => (p && {
      class: "Level3",
      context: "",
      method: "",
      path: "",
    }) as Webpb.WebpbMeta;
  }

  static create(p: ILevel3): Level3 {
    return new Level3(p);
  }

  static fromAlias(data: Record<string, unknown>): Level3 {
    return Level3.create(data as any);
  }

  toWebpbAlias(): unknown {
    return this;
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

  protected constructor(p?: ILevel2) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () => (p && {
      class: "Level2",
      context: "",
      method: "",
      path: "",
    }) as Webpb.WebpbMeta;
  }

  static create(p: ILevel2): Level2 {
    return new Level2(p);
  }

  static fromAlias(data: Record<string, unknown>): Level2 {
    const p = Webpb.toAlias(data, {
      "b": "test2",
    });
    p.test2 && (p.test2 = Level3.fromAlias(p.test2));
    p.test3 && (p.test3 = p.test3.map(e => Level3.fromAlias(e)));
    return Level2.create(p);
  }

  toWebpbAlias(): unknown {
    return Webpb.toAlias(this, {
      "test2": "b",
    });
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

  protected constructor(p?: ILevel1) {
    Webpb.assign(p, this, []);
    this.webpbMeta = () => (p && {
      class: "Level1",
      context: "",
      method: "",
      path: "",
    }) as Webpb.WebpbMeta;
  }

  static create(p: ILevel1): Level1 {
    return new Level1(p);
  }

  static fromAlias(data: Record<string, unknown>): Level1 {
    const p = Webpb.toAlias(data, {
      "a": "test1",
      "e": "test5",
    });
    p.test2 && (p.test2 = Level2.fromAlias(p.test2));
    p.test3 && (p.test3 = p.test3.map(e => Level2.fromAlias(e)));
    p.test4 && (p.test4 = Level3.fromAlias(p.test4));
    p.test5 && (p.test5 = Webpb.mapValues(p.test5, e => Level3.fromAlias(e)));
    p.test6 && (p.test6 = Webpb.mapValues(p.test6, e => Level3.fromAlias(e)));
    return Level1.create(p);
  }

  toWebpbAlias(): unknown {
    return Webpb.toAlias(this, {
      "test1": "a",
      "test5": "e",
    });
  }
}
