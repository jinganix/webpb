import { fooFromName } from "../proto3_enumeration/TsNumericProto.ts";

export function lookupFoo(name: "a" | "b" | "c"): number {
  return fooFromName(name);
}
