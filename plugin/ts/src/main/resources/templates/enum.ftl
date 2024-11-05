export enum ${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}

export const ${className}Values = [
<#list enums as enum>
  ${className}.${enum.name},
</#list>
];

export const enum Const${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}

export const Const${className}Values = [
<#list enums as enum>
  Const${className}.${enum.name},
</#list>
];