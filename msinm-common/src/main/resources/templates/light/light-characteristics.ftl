
<#macro phase phase>
    <#switch phase>
        <#case "F">fixed<#break>
        <#case "Fl">flash<#break>
        <#case "FFl">fixed light and flashing<#break>
        <#case "LFl">long flash<#break>
        <#case "Q">quick flash<#break>
        <#case "VQ">very quick flash<#break>
        <#case "IQ">interrupted quick flash<#break>
        <#case "IVQ">interrupted very quick flash<#break>
        <#case "UQ">ultra quick flash<#break>
        <#case "IUQ">interrupted ultra quick flash<#break>
        <#case "Iso">isophase<#break>
        <#case "Oc">occulting<#break>
        <#case "Alt">alternating<#break>
        <#case "Mo">morse code<#break>
    </#switch>
</#macro>

<#macro color col>
    <#switch col>
        <#case "W">white<#break>
        <#case "G">green<#break>
        <#case "R">red<#break>
        <#case "Y">yellow<#break>
        <#case "B">blue<#break>
        <#case "Am">amber<#break>
    </#switch>
</#macro>


<#macro formatlight light>
    <#if light.composite!false>
        composite groups of
    <#elseif light.grouped!false>
        groups of
    </#if>

    <#if light.groupSpec?has_content>
        <#list light.groupSpec as blinks>
            ${blinks} <#if blinks_has_next> + </#if>
        </#list>
    </#if>

    <@phase phase=light.phase />

    <#if light.colors?has_content>
        in
        <#list light.colors as col>
            <@color col=col /><#if col_has_next>, </#if>
        </#list>
    </#if>

    <#if light.phase == "Mo">
        ${light.morseCode}
    </#if>

</#macro>

<#if lightModel.light??>
    <#list lightModel.lightGroups as light>
        <@formatlight light=light /><#if light_has_next> followed by </#if>
    </#list>

    <#if lightModel.period??>
        , repeated every ${lightModel.period}. seconds
    </#if>

    <#if lightModel.elevation??>
        , the light is ${lightModel.elevation} meters above the chart datum
    </#if>

    <#if lightModel.range??>
        and is visible for ${lightModel.range} nautical miles
    </#if>

</#if>
