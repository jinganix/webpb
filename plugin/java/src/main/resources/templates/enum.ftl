// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// ${filename}

<#if package?has_content>package ${package};

</#if>
<#if imports??>
  <#list imports as import>
import ${import};
  </#list>
</#if>

<#if msgAnnos??>
  <#list msgAnnos as anno>
${anno}
  </#list>
</#if>
public enum ${className}<#if implements?has_content> implements <#list implements as implement>${implement}<#sep>, </#sep></#list></#if> {

<#list enums as enum>
  ${enum.name}(${enum.value})<#sep>,
</#sep>
</#list>;

  private ${valueType} value;

  ${className}(${valueType} value) {
    this.value = value;
  }

  public static ${className} fromValue(${valueType} value) {
    switch (value) {
<#list enums as enum>
    case ${enum.value}:
      return ${enum.name};
</#list>
    default:
      return null;
    }
  }

  @Override
  public ${valueType} getValue() {
    return this.value;
  }
}
