export enum ${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}

export const enum Const${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}