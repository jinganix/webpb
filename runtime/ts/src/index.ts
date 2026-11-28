export type Constructor<T> = {
  new (): T;
  fromAlias(data?: unknown): T;
};

type AnyObject = Record<string | number, unknown>;

export interface WebpbMessage {
  webpbMeta(): WebpbMeta;

  toWebpbAlias(): unknown;
}

export interface WebpbMeta {
  class: string;

  method: string;

  context: string;

  path: string;
}

// Framework-neutral field validation rule emitted as
// `<Message>Validation` descriptor objects when the
// `(f_opts).ts.validation_object` option is enabled.
// Only constraints declared in `(opts).opt.valid` are present.
// Usage sites bridge it to zod / class-validator / form libs, e.g.
// `{ code: { minLen: 1, maxLen: 64 } }`.
export interface WebpbValidationRule {
  required?: boolean;

  notBlank?: boolean;

  minLen?: number;

  maxLen?: number;

  min?: number | string;

  max?: number | string;

  pattern?: string;

  patternFlags?: string;

  email?: boolean;

  // Cascaded validation for nested message fields.
  valid?: boolean;
}

export type WebpbValidation = Record<string, WebpbValidationRule>;

function isPlainObject(value: unknown): value is Record<string, unknown> {
  if (value === null || typeof value !== "object" || Array.isArray(value)) {
    return false;
  }
  const prototype = Object.getPrototypeOf(value);
  return prototype === null || prototype === Object.prototype;
}

function isObject(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function getAliasConverter(value: unknown): (() => unknown) | undefined {
  if (!isObject(value)) {
    return undefined;
  }
  const record = value as Record<string, unknown>;
  if (typeof record.toAlias === "function") {
    return record.toAlias.bind(value);
  }
  if (typeof record.toWebpbAlias === "function") {
    return record.toWebpbAlias.bind(value);
  }
  return undefined;
}

function hasAliasMethod(value: unknown): boolean {
  return getAliasConverter(value) !== undefined;
}

function toAliasedValue(value: unknown): unknown {
  const convert = getAliasConverter(value);
  return convert ? convert() : value;
}

// Recursively converts nested message values inside plain objects, Maps and
// arrays, matching Go toAliasValue which recurses into slices/maps.
// Scalars and non-message class instances are returned unchanged.
function toAliasDeep(value: unknown): unknown {
  if (hasAliasMethod(value)) {
    return toAliasedValue(value);
  }
  if (Array.isArray(value)) {
    return value.map(toAliasDeep);
  }
  if (value instanceof Map) {
    const out = new Map<unknown, unknown>();
    for (const [mapKey, mapValue] of value.entries()) {
      if (typeof mapKey === "string" && isUnsafeKey(mapKey)) {
        continue;
      }
      out.set(mapKey, toAliasDeep(mapValue));
    }
    return out;
  }
  if (isPlainObject(value)) {
    const out: Record<string, unknown> = {};
    for (const [plainKey, plainValue] of Object.entries(value)) {
      if (isUnsafeKey(plainKey)) {
        continue;
      }
      out[plainKey] = toAliasDeep(plainValue);
    }
    return out;
  }
  return value;
}

function isUnsafeKey(key: string): boolean {
  return key === "__proto__" || key === "constructor" || key === "prototype";
}

function unwrapMessage(src: object): Record<string, unknown> {
  const plain: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(src)) {
    if (key === "webpbMeta" || isUnsafeKey(key)) {
      continue;
    }
    if (typeof value !== "function") {
      plain[key] = value;
    }
  }
  return plain;
}

function normalizeAssignSource(src: unknown): Record<string, unknown> | null {
  if (src === null || src === undefined) {
    return null;
  }
  if (isPlainObject(src)) {
    return src;
  }
  if (typeof src !== "object" || Array.isArray(src)) {
    return null;
  }
  const record = src as Record<string, unknown>;
  if (typeof record.toWebpbAlias === "function") {
    const aliased = record.toWebpbAlias.call(src);
    if (isPlainObject(aliased)) {
      return aliased;
    }
  }
  return unwrapMessage(src);
}

export function assign(
  src: unknown,
  dest: unknown,
  excludes: readonly string[] = [],
): void {
  const normalized = normalizeAssignSource(src);
  if (!normalized || !isObject(dest)) {
    return;
  }
  const skip = new Set(excludes);
  for (const [key, value] of Object.entries(normalized)) {
    if (isUnsafeKey(key)) {
      continue;
    }
    if (value !== undefined && !skip.has(key)) {
      dest[key] = value;
    }
  }
}

export function getter(data: unknown, path: string): unknown {
  if (data === null || data === undefined) {
    return null;
  }
  const keys = path.split(".").filter((key) => key.length > 0);
  if (keys.length === 0) {
    return data;
  }
  let current: unknown = data;
  for (const key of keys) {
    if (isUnsafeKey(key)) {
      return null;
    }
    if (!isObject(current)) {
      return null;
    }
    if (!Object.prototype.hasOwnProperty.call(current, key)) {
      return null;
    }
    current = current[key];
    if (current === null || current === undefined) {
      return null;
    }
  }
  return current;
}

export function query(pre: string, params: { [key: string]: unknown }): string {
  const queries: string[] = [];
  const pushQuery = (key: string, value: string): void => {
    if (key && value) {
      queries.push(`${key}=${encodeURIComponent(value)}`);
    }
  };
  for (const key of Object.keys(params).sort()) {
    const value = params[key];
    if (value === null || value === undefined || typeof value === "function") {
      continue;
    }
    if (Array.isArray(value)) {
      if (value.length > 0) {
        pushQuery(key, value.join(","));
      }
      continue;
    }
    if (isPlainObject(value)) {
      for (const mapKey of Object.keys(value).sort()) {
        const mapValue = value[mapKey];
        if (mapValue === undefined || mapValue === null || mapValue === "") {
          mapKey && pushQuery(key, mapKey);
        } else {
          mapKey && pushQuery(key, `${mapKey},${mapValue}`);
        }
      }
      continue;
    }
    pushQuery(key, String(value));
  }
  return queries.length ? `${pre}${queries.join("&")}` : "";
}

export function toAlias(
  data: unknown,
  aliases: { [key: string]: string },
): unknown {
  // Empty-value semantics (S3 parity note): TS keeps null (e.g.
  // toAlias({a:null}, {a:"a_"}) yields {a_:null}) and only skips undefined
  // and functions. Go ToAlias drops nil pointer/interface/map/slice fields
  // (see Go isUnset). So TS null vs Go nil is a documented gap: TS preserves
  // explicit null, Go omits nil.
  // Nested messages inside plain-object/Map/array values are converted
  // recursively via toAliasDeep, matching Go toAliasValue recursion (S2).
  if (!isObject(data)) {
    return data;
  }
  const src = isPlainObject(data) ? data : unwrapMessage(data);
  const obj: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(src)) {
    if (key === "webpbMeta" || isUnsafeKey(key)) {
      continue;
    }
    if (typeof value === "function") {
      continue;
    }
    let outKey = key;
    if (Object.prototype.hasOwnProperty.call(aliases, key) && aliases[key]) {
      const alias = aliases[key];
      if (isUnsafeKey(alias)) {
        continue;
      }
      outKey = alias;
    }
    obj[outKey] = toAliasDeep(value);
  }
  return obj;
}

export function mapValues(
  record: AnyObject | null | undefined,
  mapping: (v: unknown) => unknown,
): AnyObject {
  if (!isPlainObject(record)) {
    return {};
  }
  return Object.entries(record).reduce((acc, [key, value]) => {
    if (isUnsafeKey(key)) {
      return acc;
    }
    acc[key] = mapping(value);
    return acc;
  }, {} as AnyObject);
}
