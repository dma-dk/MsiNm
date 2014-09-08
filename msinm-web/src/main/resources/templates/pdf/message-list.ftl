<#include "message-support.ftl"/>

<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${text("list.title")}</title>

    <@messageStyles />

</head>
<body>

<#assign areaHeadingId=-9999999 />

<#function areaHeading area="area" >
  <#if area?? && (!area.parent?? || (area.parent?? && !area.parent.parent??))>
      <#return area/>
  </#if>
  <#if area?? >
      <#return areaHeading(area.parent) />
  </#if>
  <#return NULL/>
</#function>

<h1>${text("list.title")}</h1>
<table class="message-table">
    <#list messages as msg>

        <#assign area=areaHeading(msg.area) />
        <#if areaHeadings && area?? && area.id != areaHeadingId>
            <#assign areaHeadingId=area.id />
            <tr>
                <td colspan="2"><h4 style="color: #8f2f7b; font-size: 16px;"><@areaLineage area=areaHeading(area) /></h4></td>
            </tr>
        </#if>
        <tr>
            <td class="table-image">
                <img src="/message-map-image/${msg.id?c}.png" width="120" height="120"/>
            </td>
            <td class="table-item">

                <@renderMessage msg=msg />

            </td>
        </tr>
    </#list>
</table>

</body>
</html>
