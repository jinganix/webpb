import { describe, expect, it } from "vitest";
import { assign, getter, mapValues, query, toAlias } from "../src";

describe("assign", () => {
  it.each([
    {
      dest: {},
      excludes: undefined,
      expected: {},
      src: null,
      when: "src is null",
    },
    {
      dest: {},
      excludes: [],
      expected: { a: 1 },
      src: { a: 1 },
      when: "src has properties and excludes is empty",
    },
    {
      dest: {},
      excludes: ["b"],
      expected: { a: 1 },
      src: { a: 1, b: 2 },
      when: "excludes omits matching keys",
    },
  ])("should assign into dest when $when", ({ src, dest, excludes, expected }) => {
    assign(src, dest, excludes);
    expect(dest).toStrictEqual(expected);
  });
});

describe("getter", () => {
  it.each([
    { data: null, expected: null, path: "a", when: "data is null" },
    { data: undefined, expected: null, path: "a", when: "data is undefined" },
    { data: "", expected: null, path: "a", when: "data is empty string" },
    { data: [], expected: null, path: "a", when: "data is empty array" },
    { data: 0, expected: null, path: "a", when: "data is zero" },
    { data: { a: 1 }, expected: 1, path: "a", when: "path matches top-level key" },
    {
      data: { a: { b: 1 } },
      expected: 1,
      path: "a.b",
      when: "path matches nested key",
    },
    { data: { a: 1 }, expected: null, path: "b", when: "path does not exist" },
  ])("should return $expected when $when", ({ data, path, expected }) => {
    expect(getter(data, path)).toStrictEqual(expected);
  });
});

describe("query", () => {
  it.each([
    { expected: "", params: {}, pre: "?", when: "params are empty and pre is ?" },
    { expected: "", params: {}, pre: "&", when: "params are empty and pre is &" },
    {
      expected: "a=hello",
      params: { a: "hello" },
      pre: "",
      when: "param is a string",
    },
    { expected: "a=1", params: { a: 1 }, pre: "", when: "param is a number" },
    {
      expected: "?a=1",
      params: { a: 1 },
      pre: "?",
      when: "pre is ? and param is a number",
    },
    {
      expected: "?a=1&b=2",
      params: { a: 1, b: 2 },
      pre: "?",
      when: "multiple params are present",
    },
    {
      expected: "?a=1&b=2",
      params: { a: 1, b: 2, c: null, d: undefined, e: "" },
      pre: "?",
      when: "nullish and empty values are omitted",
    },
    {
      expected: "?b=2",
      params: { a: [], b: 2 },
      pre: "?",
      when: "empty array param is omitted",
    },
    {
      expected: "?a=1",
      params: { a: [1] },
      pre: "?",
      when: "single-element array is serialized",
    },
    {
      expected: "?a=1%2C2",
      params: { a: [1, 2] },
      pre: "?",
      when: "multi-element array is comma-encoded",
    },
    {
      expected: "a=1",
      params: { a: 1, b: () => "hello" },
      pre: "",
      when: "function params are omitted",
    },
    {
      expected: "a=1%2C2",
      params: { a: [1, 2] },
      pre: "",
      when: "pre is empty and array is comma-encoded",
    },
    {
      expected: "sort=a%2Cdesc&sort=b%2Casc",
      params: { sort: { a: "desc", b: "asc" } },
      pre: "",
      when: "sort object expands to repeated keys",
    },
    {
      expected: "",
      params: { sort: { "": 123 } },
      pre: "",
      when: "sort key is empty",
    },
    {
      expected: "sort=a",
      params: { sort: { a: "" } },
      pre: "",
      when: "sort value is empty string",
    },
  ])("should build query string when $when", ({ pre, params, expected }) => {
    expect(query(pre, params)).toStrictEqual(expected);
  });
});

describe("toAlias", () => {
  it.each([
    {
      aliases: {},
      data: null,
      expected: null,
      when: "data is null",
    },
    {
      aliases: {},
      data: "",
      expected: "",
      when: "data is empty string",
    },
    {
      aliases: {},
      data: [],
      expected: [],
      when: "data is empty array",
    },
    {
      aliases: {},
      data: [1, 2],
      expected: [1, 2],
      when: "data is non-empty array",
    },
    {
      aliases: { a: "a_" },
      data: { a: null },
      expected: { a_: null },
      when: "aliases rename object keys",
    },
  ])("should return aliased value when $when", ({ data, aliases, expected }) => {
    expect(toAlias(data, aliases)).toStrictEqual(expected);
  });

  it("should call toAlias recursively when property has toAlias method", () => {
    // Given
    const b: Record<string, unknown> = { b: 2, c: 3 };
    b["toAlias"] = () => toAlias(b, { b: "b_" });
    const a: Record<string, unknown> = { a: 1, b: b, c: 3 };

    // When
    const withAliases = toAlias(a, { a: "a_" });
    const withoutAliases = toAlias(a, {});

    // Then
    expect(withAliases).toMatchObject({
      a_: 1,
      b: { b_: 2, c: 3 },
      c: 3,
    });
    expect(withoutAliases).toMatchObject({
      a: 1,
      b: { b_: 2, c: 3 },
      c: 3,
    });
  });
});

describe("mapValues", () => {
  it("should convert all values to string when mapper returns string", () => {
    expect(mapValues({ a: 1, b: true }, (v) => String(v))).toMatchObject({
      a: "1",
      b: "true",
    });
  });
});
