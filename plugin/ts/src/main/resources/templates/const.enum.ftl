export const enum ${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}

export enum Enum${className} {
<#list enums as enum>
  ${enum.name} = ${enum.value},
</#list>
}