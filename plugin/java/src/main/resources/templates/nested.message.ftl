<#if msgAnnos??>
  <#list msgAnnos as anno>
${anno}
  </#list>
</#if>
public static class ${className}<#if extend??> extends ${extend}</#if ><#if implements?has_content> implements <#list implements as implement>${implement}<#sep>, </#sep></#list></#if> {

<#include "/message.body.ftl">
}