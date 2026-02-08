import { describe, expect, it } from "vitest";
import { assign, getter, mapValues, query, toAlias } from "../src";

describe("index", () => {
  describe("assign", () => {
    [
      { dest: {}, excludes: undefined, expected: {}, src: null },
      { dest: {}, excludes: [], expected: { a: 1 }, src: { a: 1 } },
      { dest: {}, excludes: ["b"], expected: { a: 1 }, src: { a: 1, b: 2 } },
    ].forEach(({ src, dest, excludes, expected }) => {
      it(`assign(${JSON.stringify(src)}, ${JSON.stringify(dest)}) => ${JSON.stringify(expected)}`, () => {
        assign(src, dest, excludes);
        expect(dest).toStrictEqual(expected);
      });
    });
  });

  describe("getter", () => {
    [
      { data: null, expected: null, path: "a" },
      { data: undefined, expected: null, path: "a" },
      { data: "", expected: null, path: "a" },
      { data: [], expected: null, path: "a" },
      { data: 0, expected: null, path: "a" },
      { data: { a: 1 }, expected: 1, path: "a" },
      { data: { a: { b: 1 } }, expected: 1, path: "a.b" },
      { data: { a: 1 }, expected: null, path: "b" },
    ].forEach(({ data, path, expected }) => {
      it(`getter(${JSON.stringify(data)}, "${path}") => ${JSON.stringify(expected)}`, () => {
        expect(getter(data, path)).toStrictEqual(expected);
      });
    });
  });

  describe("query", () => {
    [
      { expected: "", params: {}, pre: "?" },
      { expected: "", params: {}, pre: "&" },
      { expected: "a=hello", params: { a: "hello" }, pre: "" },
      { expected: "a=1", params: { a: 1 }, pre: "" },
      { expected: "?a=1", params: { a: 1 }, pre: "?" },
      { expected: "?a=1&b=2", params: { a: 1, b: 2 }, pre: "?" },
      {
        expected: "?a=1&b=2",
        params: { a: 1, b: 2, c: null, d: undefined, e: "" },
        pre: "?",
      },
      { expected: "?b=2", params: { a: [], b: 2 }, pre: "?" },
      { expected: "?a=1", params: { a: [1] }, pre: "?" },
      { expected: "?a=1%2C2", params: { a: [1, 2] }, pre: "?" },
      { expected: "a=1", params: { a: 1, b: () => "hello" }, pre: "" },
      { expected: "a=1%2C2", params: { a: [1, 2] }, pre: "" },
      {
        expected: "sort=a%2Cdesc&sort=b%2Casc",
        params: { sort: { a: "desc", b: "asc" } },
        pre: "",
      },
      { expected: "", params: { sort: { "": 123 } }, pre: "" },
      { expected: "sort=a", params: { sort: { a: "" } }, pre: "" },
    ].forEach(({ pre, params, expected }) => {
      it(`query(${JSON.stringify(pre)}, "${JSON.stringify(params)}") => ${JSON.stringify(expected)}`, () => {
        expect(query(pre, params)).toStrictEqual(expected);
      });
    });
  });

  describe("toAlias", () => {
    [
      { aliases: {}, data: null, expected: null },
      { aliases: {}, data: "", expected: "" },
      { aliases: {}, data: [], expected: [] },
      { aliases: {}, data: [1, 2], expected: [1, 2] },
      { aliases: { a: "a_" }, data: { a: null }, expected: { a_: null } },
    ].forEach(({ data, aliases, expected }) => {
      it(`getter(${JSON.stringify(data)}, "${JSON.stringify(aliases)}") => ${JSON.stringify(expected)}`, () => {
        expect(toAlias(data, aliases)).toStrictEqual(expected);
      });
    });

    describe("when property has toAlias method", () => {
      it("should call toAlias recursively", () => {
        const b: Record<string, unknown> = { b: 2, c: 3 };
        b["toAlias"] = () => toAlias(b, { b: "b_" });
        const a: Record<string, unknown> = { a: 1, b: b, c: 3 };
        expect(toAlias(a, { a: "a_" })).toMatchObject({
          a_: 1,
          b: { b_: 2, c: 3 },
          c: 3,
        });
        expect(toAlias(a, {})).toMatchObject({
          a: 1,
          b: { b_: 2, c: 3 },
          c: 3,
        });
      });
    });
  });

  describe("mapValues", () => {
    describe("when mapping to string", () => {
      it("should convert all values to string", () => {
        expect(mapValues({ a: 1, b: true }, (v) => String(v))).toMatchObject({
          a: "1",
          b: "true",
        });
      });
    });
  });
});
