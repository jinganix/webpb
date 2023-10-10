export interface I${className}<#if extend??> extends ${extendI}</#if> {
<#list fields as field>
  ${field.name}<#if field.optional>?</#if>: ${field.type};
</#list>
}

export class ${className}<#if extend??> extends ${extend}</#if> implements I${className}, Webpb.WebpbMessage {
<#list fields as field>
  ${field.name}<#if !field.default??>${field.optional?then("?", "!")}</#if>: ${field.type}<#if field.default??> = ${field.default}</#if>;
</#list>
  webpbMeta: () => Webpb.WebpbMeta;

<#if fields?has_content>
  protected constructor(p?: I${className}) {
<#if extend??>    super();
</#if>    Webpb.assign(p, this, [<#list omitted as o>"${o}"<#sep>, </#sep></#list>]);
    this.webpbMeta = () => (p && {
<#else>
  protected constructor() {
<#if extend??>    super();
</#if>    this.webpbMeta = () => ({
</#if>
      class: "${className}",
      context: "${context}",
      method: "${method}",
<#if path?has_content>
      path: `<#if path.url??>${path.url}</#if><#if path.queries?has_content>${r'${Webpb.query("?", {'}
      <#list path.queries as query>
        "${query.key}": ${query.value},
      </#list>
      })}</#if>`
<#else>
      path: "",
</#if>
    }) as Webpb.WebpbMeta;
  }

  static create(<#if fields?has_content>p: I${className}</#if>): ${className} {
    return new ${className}(<#if fields?has_content>p</#if>);
  }

<#if fields?has_content>
  static fromAlias(data: Record<string, unknown>): ${className} {
    <#if hasAlias>
        <#if aliases?has_content>
    const p = Webpb.toAlias(data, {
            <#list aliases?keys as key>
      "${aliases[key]}": "${key}",
            </#list>
    });
        <#else>
    const p = Webpb.toAlias(data, {});
        </#if>
        <#list aliasMsgs as alias>
            <#if alias.collection == "map">
    p.${alias.name} && (p.${alias.name} = Webpb.mapValues(p.${alias.name}, e => ${alias.type}.fromAlias(e)));
            <#elseif alias.collection == "list">
    p.${alias.name} && (p.${alias.name} = p.${alias.name}.map(e => ${alias.type}.fromAlias(e)));
            <#else>
    p.${alias.name} && (p.${alias.name} = ${alias.type}.fromAlias(p.${alias.name}));
            </#if>
        </#list>
    return ${className}.create(p);
    <#else>
    return ${className}.create(data as any);
    </#if>
  }
<#else>
  static fromAlias(_data?: Record<string, unknown>): ${className} {
    return ${className}.create();
  }
</#if>

  toWebpbAlias(): unknown {
<#if hasAlias>
    <#if aliases?has_content>
    return Webpb.toAlias(this, {
    <#list aliases?keys as key>
      "${key}": "${aliases[key]}",
    </#list>
    });
    <#else>
    return Webpb.toAlias(this, {});
    </#if>
<#else>
    return this;
</#if>
  }
}<#if nestedMsgs?has_content>

export namespace ${className} {
<#list nestedMsgs as nested>
${nested}
<#sep>

</#sep>
</#list>
}</#if>