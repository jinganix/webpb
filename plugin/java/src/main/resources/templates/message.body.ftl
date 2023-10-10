  public static final String WEBPB_METHOD = "${method}";

  public static final String WEBPB_CONTEXT = "${context}";

  public static final String WEBPB_PATH = "${path}";

  public static final ${webpbMeta} WEBPB_META = new ${webpbMeta}.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public ${webpbMeta} webpbMeta() {
    return WEBPB_META;
  }
<#list fields as field>

  <#if field.annos??>
    <#list field.annos as anno>
  ${anno}
    </#list>
  </#if>
  <#if field.default??>
  private ${field.type} ${field.name} = ${field.default};
  <#else>
  private ${field.type} ${field.name};
  </#if>
</#list>

  public ${className}() {
  }
<#if fields?? && fields?size gt 0 && fields?size lte 5>

  public ${className}(<#list fields as field>${field.type} ${field.name}<#sep>, </#sep></#list>) {
  <#list fields as field>
    this.${field.name} = ${field.name};
  </#list>
  }
</#if>
<#if genGetter || genSetter>
<#list fields as field>

  <#if genGetter>
  public ${field.type} get${field.name?cap_first}() {
    return this.${field.name};
  }
  </#if>

  <#if genSetter>
  public ${className} set${field.name?cap_first}(${field.type} ${field.name}) {
    this.${field.name} = ${field.name};
    return this;
  }
  </#if>
</#list>
</#if>
<#list nestedMsgs as nested>

${nested}
</#list>