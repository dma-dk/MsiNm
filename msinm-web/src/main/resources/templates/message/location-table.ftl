
<#assign formatPos = "dk.dma.msinm.templates.LatLonDirective"?new()>

<#if message.locations?has_content>
    <#list message.locations as loc>
        <#if loc.descs?has_content && loc.descs[0].description?has_content>
            <p>${loc.descs[0].description}</p>
        </#if>
        <table width="100%">
            <tr>
                <th>${text("latitude")}</th>
                <th>${text("longitude")}</th>
                <th>${text("description")}</th>
            </tr>
            <#list loc.points as point>
                <tr>
                    <td><@formatPos lat=point.lat /></td>
                    <td><@formatPos lon=point.lon /></td>
                    <td><#if point.descs?has_content && point.descs[0].description?has_content>${point.descs[0].description}</#if></td>
                </tr>
            </#list>
        </table>
    </#list>
</#if>
