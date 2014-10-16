<#assign line = "dk.dma.msinm.common.templates.LineDirective"?new()>
<#assign formatPos = "dk.dma.msinm.templates.LatLonDirective"?new()>
<#assign htmlToText = "dk.dma.msinm.common.templates.HtmlToTextDirective"?new()>

<#macro areaLineage area>
    <#if area??>
        <#if area.parent?? && area.parent.parent??>
            <@areaLineage area=area.parent /> -
        </#if>
        <#if area.descs?has_content>${area.descs[0].name}</#if>
    </#if>
</#macro>

<@line>
    ${msg.seriesIdentifier.fullId}:
    <#if msg.area??>
        <@areaLineage area=msg.area />
    </#if>
    <#if msg.descs?has_content && msg.descs[0].vicinity?has_content>
        - ${msg.descs[0].vicinity}
    </#if>
    <#if msg.descs?has_content && msg.descs[0].title?has_content>
        - ${msg.descs[0].title}
    </#if>
</@line>


<#if msg.references?has_content>
References:
<#list msg.references as ref>
    <@line>
        * ${ref.seriesIdentifier.fullId}
        <#if ref.type == 'REPETITION'>
            (repitition)
        <#elseif ref.type == 'CANCELLATION'>
            (cancellation)
        <#elseif ref.type == 'UPDATE'>
            (update)
        <#elseif ref.type == 'REFERENCE'>
            (reference)
        </#if>
    </@line>
</#list>

</#if>

<#if msg.charts?has_content>
    <@line>
        Charts:
        <#list msg.charts as chart>
            ${chart.chartNumber}
            <#if chart.internationalNumber?has_content>(INT ${chart.internationalNumber?c})</#if>
            <#if chart_has_next>, </#if>
        </#list>
    </@line>

</#if>

<#if msg.locations?has_content>
Positions:
<@line>
        <#list msg.locations as loc>
            <#if loc.descs?has_content && loc.descs[0].description?has_content>
                ${loc.descs[0].description}.
            </#if>
            <#list loc.points as point>
                <@formatPos lat=point.lat lon=point.lon format="navtex"/>
                <#if point.descs?has_content && point.descs[0].description?has_content> (${point.descs[0].description})</#if>
                <#if point_has_next> and </#if>
            </#list>
            <#if loc_has_next> and location </#if>
        </#list>
</@line>

</#if>

<#if msg.descs?has_content && msg.descs[0].description?has_content>
Details:

<@htmlToText html=msg.descs[0].description />

</#if>

<#if msg.validTo?has_content>
<@line>
    Cancel this message ${msg.validTo?datetime}.
</@line>

</#if>
