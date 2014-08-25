
<#assign formatPos = "dk.dma.msinm.templates.LatLonDirective"?new()>

<#if message.locations?has_content>
    <#list message.locations as loc>
        <p>
            <#if loc.descs?has_content && loc.descs[0].description?has_content>
                ${loc.descs[0].description}<br/>
            </#if>
            <#list loc.points as point>
                <@formatPos lat=point.lat lon=point.lon /><#if point.descs?has_content && point.descs[0].description?has_content>, ${point.descs[0].description}</#if><br/>
            </#list>
        </p>
    </#list>
</#if>
