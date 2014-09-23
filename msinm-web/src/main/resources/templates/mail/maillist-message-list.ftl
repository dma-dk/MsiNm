<#import "maillist-template.ftl" as mail/>
<#include "../pdf/message-support.ftl"/>

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

<@mail.page title="${mailList.name}">

    <p>${text("dear.user", name)}</p>

    <p>${text("message.list.intro", mailList.name, total)}</p>

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
                    <a href="${baseUri}/index.html#/message/${msg.id?c}" target="_blank">
                        <img src="/message-map-image/${msg.id?c}.png" width="120" height="120"/>
                    </a>
                </td>
                <td class="table-item">

                    <@renderMessage msg=msg />

                </td>
            </tr>
        </#list>
    </table>

</@mail.page>
