
<#assign formatPos = "dk.dma.msinm.templates.LatLonDirective"?new()>

<#if message.locations?has_content>
    <ol>
    <#list message.locations as loc>
        <li>
            <#if loc.descs?has_content && loc.descs[0].description?has_content>
                <p>${loc.descs[0].description}</p>
            </#if>
            <p>
            <#list loc.points as point>
                <@formatPos lat=point.lat lon=point.lon /><#if point.descs?has_content && point.descs[0].description?has_content> (${point.descs[0].description})</#if><br/>
            </#list>
            </p>
        </li>
    </#list>
    </ol>
</#if>
