<#import "mail-template.ftl" as mail/>
<@mail.page title="MSI-NM Report">

<style>
    .table-image {
        vertical-align: top;
        padding: 10px;
        border-top: 1px solid lightgray;
    }

    .table-item {
        vertical-align: top;
        width: 100%;
        padding: 10px 10px 10px 40px;
        border-top: 1px solid lightgray;
    }

    .field-name {
        white-space: nowrap;
        vertical-align: top;
        font-style: italic;
        padding-right: 10px;
        text-align: right;
    }

    .field-value {
        vertical-align: top;
        width: 100%;
    }

    .field-value ol {
        padding-left: 0;
    }

    .attachment {
        text-align: center;
    }

    .attachment-image {
        display: inline-block;
        vertical-align: middle;
    }

    .attachment-image img {
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
    }

    .attachment-label {
        white-space: nowrap;
        overflow: hidden;
        font-size: 10px;
    }

    .size-32 {
        width: 32px;
        height: 32px;
        line-height: 32px;
    }

</style>

<#macro areaLineage area>
    <#if area??>
        <#if area.parent??>
            <@areaLineage area=area.parent /> -
        </#if>
        <#if area.descs?has_content>${area.descs[0].name}</#if>
    </#if>
</#macro>

<#assign formatPos = "dk.dma.msinm.templates.LatLonDirective"?new()>
<#assign txtToHtml = "dk.dma.msinm.common.templates.TextToHtmlDirective"?new()>

<table>
    <tr>

        <td class="table-image">
            <img src="/report-map-image/${report.id?c}.png" width="120" height="120"/>            
        </td>
        
        <td class="table-item">

            <table>
                <!-- Created line -->
                <tr>
                    <td class="field-name">Created</td>
                    <td class="field-value">
                        ${report.created?datetime}
                    </td>
                </tr>
    
                <!-- User line -->
                <tr>
                    <td class="field-name">User</td>
                    <td class="field-value">${report.user.email}</td>
                </tr>
    
                <!-- Contact line -->
                <#if report.contact?has_content>
                    <tr>
                        <td class="field-name">Contact</td>
                        <td class="field-value">
                            <@txtToHtml text=report.contact />
                        </td>
                    </tr>
                </#if>
    
                <!-- Area line -->
                <#if report.area?has_content>
                    <tr>
                        <td class="field-name">Area</td>
                        <td class="field-value">
                            <@areaLineage area=report.area />
                        </td>
                    </tr>
                </#if>

                <!-- Location line -->
                <#if report.locations?has_content>
                    <tr>
                        <td class="field-name">Location</td>
                        <td class="field-value">
                            <#list report.locations as loc>
                                <#if loc.descs?has_content>
                                    <div>${loc.descs[0].description}</div>
                                </#if>
                                <#list loc.points as point>
                                    <div>
                                        <@formatPos lat=point.lat lon=point.lon /><#if point.descs?has_content>, ${point.descs[0].description}</#if>
                                    </div>
                                </#list>
                            </#list>
                        </td>
                    </tr>
                </#if>

                <!-- Description line -->
                <#if report.description?has_content>
                    <tr>
                        <td class="field-name">Details</td>
                        <td class="field-value">
                            <@txtToHtml text=report.description />
                        </td>
                    </tr>
                </#if>

                <!-- Attachments -->
                <#if attachments?has_content>
                    <tr>
                        <td class="field-name">Attachments</td>
                        <td class="field-value">
                            <table>
                                <tr>
                                    <#list attachments as file>
                                        <td class="attachment">
                                            <div class="attachment-image size-32">
                                                <a href="/rest/repo/file/${file.path}" target="_blank">
                                                    <img src="/rest/repo/thumb/${file.path}?size=32">
                                                </a>
                                            </div>
                                            <div class="attachment-label">
                                                ${file.name}
                                            </div>
                                        </td>
                                    </#list>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </#if>
            </table>
        </td>

    </tr>
</table>


</@mail.page>
