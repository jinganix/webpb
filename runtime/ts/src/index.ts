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

function unwrapMessage(src: object): Record<string, unknown> {
  const plain: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(src)) {
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
    if (!isObject(current)) {
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
  for (const [key, value] of Object.entries(params)) {
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
      for (const mapKey of Object.keys(value)) {
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
  if (!isPlainObject(data)) {
    return data;
  }
  const obj: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(data)) {
    if (hasAliasMethod(value)) {
      obj[key] = toAliasedValue(value);
    } else if (aliases[key]) {
      obj[aliases[key]] = value;
    } else {
      obj[key] = value;
    }
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
    acc[key] = mapping(value);
    return acc;
  }, {} as AnyObject);
}
