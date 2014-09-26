<#assign now = .now>
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

<@line maxLength=40 case="upper">
    ${dateFormat.format(now)}
</@line>

<@line maxLength=40 case="upper">
    ${msg.type} ${msg.seriesIdentifier.fullId}
</@line>

<@line maxLength=40 case="upper">
    <#if msg.area??>
        <@areaLineage area=msg.area />
    </#if>
    <#if msg.descs?has_content && msg.descs[0].vicinity?has_content>
        - ${msg.descs[0].vicinity}
    </#if>
</@line>

<#if msg.charts?has_content>
    <@line maxLength=40 case="upper">
        charts
        <#list msg.charts as chart>
            ${chart.chartNumber}
            <#if chart.internationalNumber?has_content>(INT ${chart.internationalNumber?c})</#if>
            <#if chart_has_next>, </#if>
        </#list>
    </@line>
</#if>

<#if msg.locations?has_content>
    <@line maxLength=40 case="upper">
        Pos
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

<#if msg.descs?has_content && msg.descs[0].title?has_content>
    <@line maxLength=40 case="upper">
        ${msg.descs[0].title}
    </@line>
</#if>

<#if msg.descs?has_content && msg.descs[0].description?has_content>
    <@line maxLength=40 case="upper">
        <@htmlToText html=msg.descs[0].description />
    </@line>
</#if>

<#if msg.descs?has_content && msg.descs[0].note?has_content>
    <@line maxLength=40 case="upper">
        ${msg.descs[0].note}
    </@line>
</#if>

<#if msg.references?has_content>
    <#list msg.references as ref>
        <#if ref.type == 'CANCELLATION'>
            <@line maxLength=40 case="upper">
                Cancel message ${ref.seriesIdentifier.fullId}.
            </@line>

        </#if>
    </#list>
</#if>

<#if msg.validTo?has_content>
    <@line maxLength=40 case="upper">
        Cancel this message ${dateFormat.format(msg.validTo)}.
    </@line>
</#if>

<#if navtex.priority?has_content>

PRIORITY:
${navtex.priority}
</#if>