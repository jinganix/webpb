import { describe, expect, it } from "vitest";
import { assign, getter, mapValues, query, toAlias } from "../src";

describe("round1", () => {
  it("toAlias accepts class instance, skips functions and webpbMeta", () => {
    class Msg {
      a = 1;
      b = 2;
      fn = () => 1;
      webpbMeta = () => ({});
    }
    const result = toAlias(new Msg(), { a: "a_" }) as Record<string, unknown>;
    expect(result).toStrictEqual({ a_: 1, b: 2 });
  });
  it("assign skips __proto__/constructor/prototype", () => {
    for (const k of ["__proto__", "constructor", "prototype"]) {
      const src = JSON.parse(`{"${k}":{"polluted":true},"a":1}`);
      const dest: Record<string, unknown> = {};
      assign(src, dest);
      expect((dest as Record<string, unknown>).polluted).toBeUndefined();
      expect(Object.getPrototypeOf(dest)).toBe(Object.prototype);
      expect(Object.prototype.hasOwnProperty.call(dest, k)).toBe(false);
    }
    const src = JSON.parse('{"__proto__":{"polluted":true},"a":1}');
    const dest: Record<string, unknown> = {};
    assign(src, dest);
    expect(dest).toStrictEqual({ a: 1 });
  });
  it("toAlias skips __proto__/constructor/prototype", () => {
    for (const k of ["__proto__", "constructor", "prototype"]) {
      const src = JSON.parse(`{"${k}":{"polluted":true},"a":1}`);
      const result = toAlias(src, {}) as Record<string, unknown>;
      expect((result as Record<string, unknown>).polluted).toBeUndefined();
      expect(Object.getPrototypeOf(result)).toBe(Object.prototype);
      expect(Object.prototype.hasOwnProperty.call(result, k)).toBe(false);
    }
  });
  it("mapValues skips __proto__/constructor/prototype", () => {
    for (const k of ["__proto__", "constructor", "prototype"]) {
      const record = JSON.parse(`{"${k}":{"polluted":true},"a":2}`);
      const result = mapValues(record, (v) => v);
      expect((result as Record<string, unknown>).polluted).toBeUndefined();
      expect(Object.getPrototypeOf(result)).toBe(Object.prototype);
      expect(Object.prototype.hasOwnProperty.call(result, k)).toBe(false);
    }
    const record = JSON.parse('{"__proto__":{"polluted":true},"a":2}');
    expect(mapValues(record, (v) => v)).toStrictEqual({ a: 2 });
  });
  it("query matches Go sorted output for same params", () => {
    // Keep insertion order sorted so TS/Go produce identical URLs.
    expect(query("?", { a: "1", b: "2", c: "3" })).toBe("?a=1&b=2&c=3");
    expect(query("", { sort: { a: "desc", b: "asc" } })).toBe(
      "sort=a%2Cdesc&sort=b%2Casc",
    );
  });
  it("query sorts unsorted outer/inner keys like Go sort.Strings", () => {
    // Cross-language lock: same params as Go TestQuerySorted must give same URL.
    expect(query("?", { c: "3", b: "2", a: "1" })).toBe("?a=1&b=2&c=3");
    expect(query("?", { b: "2", a: "1" })).toBe("?a=1&b=2");
    expect(query("", { sort: { b: "asc", a: "desc" } })).toBe(
      "sort=a%2Cdesc&sort=b%2Casc",
    );
  });
  it("query generic slice matches Go Query reflection join", () => {
    // Cross-language lock: Go Query("?", {"ids": []any{"1","2"}}) == TS.
    expect(query("?", { ids: ["1", "2"] })).toBe("?ids=1%2C2");
    expect(query("?", { ids: [1, 2] })).toBe("?ids=1%2C2");
  });
  it("query generic map matches Go map[string]any expansion", () => {
    // M1 lock with Go TestQueryGenericMapParity: same params same URL.
    // Go: Query("?", map[string]any{"sort": map[string]any{"a": "desc"}}).
    expect(query("?", { sort: { a: "desc" } })).toBe("?sort=a%2Cdesc");
    expect(query("", { sort: { a: 1 } })).toBe("sort=a%2C1");
    // Empty (""/null) inner values only push key, like Go nil/"" branch.
    expect(query("", { sort: { a: "", b: null } })).toBe("sort=a&sort=b");
  });
  it("query skips function like Go Func/Chan skip", () => {
    // S1 lock with Go TestQuerySkipsFuncChan (chan has no TS equivalent).
    expect(query("", { a: 1, fn: () => "x", b: 2 })).toBe("a=1&b=2");
  });
  it("toAlias recurses into map<string,Message> like Go", () => {
    // S2 lock with Go TestToAliasMapMessageParity.
    const nested = { toWebpbAlias: () => ({ a: "n" }) };
    expect(toAlias({ m: { k: nested } }, {})).toStrictEqual({
      m: { k: { a: "n" } },
    });
    // Arrays of messages also recurse, matching Go slice handling.
    expect(toAlias({ m: [nested] }, {})).toStrictEqual({
      m: [{ a: "n" }],
    });
    // Map instances recurse on values, matching Go map handling.
    expect(toAlias({ m: new Map([["k", nested]]) }, {})).toStrictEqual({
      m: new Map([["k", { a: "n" }]]),
    });
  });
  it("toAlias renames key even when value has alias method", () => {
    // Parity with Go structToAlias: alias rename applies before nested convert.
    const nested = { toWebpbAlias: () => ({ aliased: true }) };
    expect(toAlias({ nested }, { nested: "n_" })).toStrictEqual({
      n_: { aliased: true },
    });
    const withToAlias: Record<string, unknown> = {
      toAlias: () => ({ aliased: true }),
    };
    expect(toAlias({ nested: withToAlias }, { nested: "n_" })).toStrictEqual({
      n_: { aliased: true },
    });
  });
  it("getter blocks unsafe keys and inherited properties", () => {
    expect(getter({ a: 1 }, "__proto__")).toBeNull();
    expect(getter({ a: 1 }, "constructor")).toBeNull();
    expect(getter({ a: 1 }, "prototype")).toBeNull();
    expect(getter({ a: 1 }, "toString")).toBeNull();
    expect(getter({ a: { b: 1 } }, "a.__proto__")).toBeNull();
  });
});
