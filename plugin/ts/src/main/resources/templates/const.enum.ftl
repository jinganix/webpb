export const enum ${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}

export const ${className}Values = [
<#list enums as enum>
  ${className}.${enum.name},
</#list>
];

export enum Enum${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}

export const Enum${className}Values = [
<#list enums as enum>
  Enum${className}.${enum.name},
</#list>
];