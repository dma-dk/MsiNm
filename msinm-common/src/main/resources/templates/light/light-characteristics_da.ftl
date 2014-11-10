
<#macro phase phase>
    <#switch phase>
        <#case "F">fast lys<#break>
        <#case "Fl">blink<#break>
        <#case "FFl">fast lys med blink<#break>
        <#case "LFl">lange blink<#break>
        <#case "Q">uafbrudte hurtigblink<#break>
        <#case "VQ">uafbrudte meget hurtige blink<#break>
        <#case "IQ">afbrudte hurtigblink<#break>
        <#case "IVQ">afbrudte meget hurtige blink<#break>
        <#case "UQ">uafbrudte ultra-hurtige blink<#break>
        <#case "IUQ">afbrudte ultra-hurtige blink<#break>
        <#case "Iso">isofase blink<#break>
        <#case "Oc">formørkelser<#break>
        <#case "Alt">alternerende lys<#break>
        <#case "Mo">blink i morsekode<#break>
    </#switch>
</#macro>

<#macro color col>
    <#switch col>
        <#case "W">hvid<#break>
        <#case "G">grøn<#break>
        <#case "R">rød<#break>
        <#case "Y">gul<#break>
        <#case "B">blå<#break>
        <#case "Am">ravgul<#break>
    </#switch>
</#macro>


<#macro formatlight light>
    <#if light.composite!false>
        sammensatte grupper af
    <#elseif light.grouped!false>
        grupper af
    </#if>

    <#if light.groupSpec?has_content>
        <#list light.groupSpec as blinks>
            ${blinks} <#if blinks_has_next> + </#if>
        </#list>
    </#if>

    <@phase phase=light.phase />

    <#if light.colors?has_content>
        i
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
        <@formatlight light=light /><#if light_has_next> efterfulgt af </#if>
    </#list>

    <#if lightModel.period??>
        , som gentages hver ${lightModel.period}. sekund
    </#if>

    <#if lightModel.elevation??>
        , lyset er ${lightModel.elevation} meter over kort-datum
    </#if>

    <#if lightModel.range??>
        og er synlig over ${lightModel.range} nautiske mil
    </#if>

</#if>
