export type Constructor<T> = {
  new (): T;
  fromAlias<T, D>(this: Constructor<T>, data: Partial<D>): T;
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

export function assign(src: unknown, dest: unknown, excludes?: string[]): void {
  if (!src || !dest || typeof src !== "object" || typeof dest !== "object") {
    return;
  }
  for (const [key, value] of Object.entries(src)) {
    if (value !== undefined && !isExcluded(key, excludes)) {
      (dest as Record<string, unknown>)[key] = value;
    }
  }
}

function isExcluded(k: string, excludes?: string[]): boolean {
  if (!excludes) {
    return false;
  }
  for (const o of excludes) {
    if (o === k) {
      return true;
    }
  }
  return false;
}

export function getter(data: unknown, path: string): unknown {
  if (data === null || data === undefined || typeof data !== "object") {
    return null;
  }
  for (const key of path.split(".")) {
    data = (data as Record<string, unknown>)[key];
    if (data === null || data === undefined) {
      return data;
    }
  }
  return data;
}

export function query(pre: string, params: { [key: string]: unknown }): string {
  const queries: string[] = [];
  for (const [key, value] of Object.entries(params)) {
    if (value === null || value === undefined || typeof value === "function") {
      continue;
    }
    const encoded = encodeURIComponent(String(value));
    if (encoded) {
      queries.push(`${key}=${encoded}`);
    }
  }
  return queries.length ? `${pre}${queries.join("&")}` : "";
}

export function toAlias(
  data: unknown,
  aliases: { [key: string]: string },
): unknown {
  if (!data || typeof data !== "object" || Array.isArray(data)) {
    return data;
  }
  const obj: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(data)) {
    const toAlias = (value as { toAlias: () => unknown })?.toAlias;
    if (typeof toAlias === "function") {
      obj[key] = toAlias();
    } else if (aliases && aliases[key]) {
      obj[aliases[key]] = value;
    } else {
      obj[key] = value;
    }
  }
  return obj;
}

export function mapValues(
  record: AnyObject,
  mapping: (v: unknown) => unknown,
): AnyObject {
  return Object.entries(record).reduce((acc, [key, value]) => {
    acc[key] = mapping(value);
    return acc;
  }, {} as AnyObject);
}
