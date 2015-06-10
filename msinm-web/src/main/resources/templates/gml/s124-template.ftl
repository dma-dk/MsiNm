<?xml version="1.0" encoding="UTF-8"?>
<NoticeBatch xmlns:gml="http://www.opengis.net/gml/3.2"
             xmlns:xlink="http://www.w3.org/1999/xlink"
             xmlns:s100="http://www.iho.int/s100gml"
             xmlns="http://jeppesen.com/KRSeaTrial"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://jeppesen.com/KRSeaTrial MSI.xsd" gml:id="WA11301">


    <DatasetMetadataProperty>
        <DatasetMetadata>
            <datasetIdentifier>${msg.id?c}</datasetIdentifier>
            <issueDate>${msg.created?datetime}</issueDate>
        </DatasetMetadata>
    </DatasetMetadataProperty>

    <Notice gml:id="${msg.id?c}_1">

        <noticeIdentifier>
            <noticeNumber>${msg.seriesIdentifier.number?c}</noticeNumber>
            <year>${msg.seriesIdentifier.year?c}</year>
            <productionAgency>${msg.seriesIdentifier.authority}</productionAgency>
            <mainType>${msg.seriesIdentifier.mainType?lower_case}</mainType>
        </noticeIdentifier>

        <sourceDate>${msg.created?datetime}</sourceDate>

        <@noticeType msg=msg />

        <#if msg.seriesIdentifier.mainType == 'MSI'>
            <navigationalArea>Danish Nav Warn</navigationalArea>
        <#elseif msg.originalInformation??>
            <originalInformation>${msg.originalInformation}</originalInformation>
        </#if>

        <#if msg.area?? && msg.area.parent?? && msg.area.parent.descs?has_content>
            <generalArea>${msg.area.parent.descs[0].name}</generalArea>
        </#if>

        <#if msg.area?? && msg.area.descs?has_content>
            <locality>${msg.area.descs[0].name}</locality>
        </#if>

        <#if msg.horizontalDatum??>
            <horizontalDatum>${msg.horizontalDatum}</horizontalDatum>
        </#if>

        <#if msg.descs?has_content && msg.descs[0].title?has_content>
            <keySubject>${msg.descs[0].title}</keySubject>
        </#if>

        <@generalCategory msg=msg />

        <#if msg.references?has_content>
            <#list msg.references as ref>
                <@noticeReference ref=ref />
            </#list>
        </#if>

        <#if msg.descs?has_content && msg.descs[0].description?has_content>
            <#list msg.descs as desc>
                <information>
                    <text>${desc.description}</text>
                    <language>${desc.lang}</language>
                </information>
            </#list>
        </#if>

        <duration>
            <dateStart>${msg.validFrom?datetime}</dateStart>
            <#if msg.validTo??>
                <dateEnd>${msg.validTo?datetime}</dateEnd>
            </#if>
        </duration>

        <#if msg.charts?has_content>
            <#list msg.charts as chart>
                <@affectedChart chart=chart />
            </#list>
        </#if>

        <#if msg.locations?has_content>
            <#list msg.locations as loc>
                <#if loc.points?has_content>
                    <@location loc=loc indx=loc_index/>
                </#if>
            </#list>
        </#if>

    </Notice>


</NoticeBatch>

<#macro noticeType msg>
    <#switch msg.type>
    <#case 'COASTAL_WARNING'><typeOfNotice>coastal</typeOfNotice><#break>
    <#case 'SUBAREA_WARNING'><typeOfNotice>sub-area</typeOfNotice><#break>
    <#case 'NAVAREA_WARNING'><typeOfNotice>NAVAREA</typeOfNotice><#break>
    <#case 'LOCAL_WARNING'><typeOfNotice>local</typeOfNotice><#break>
    <#case 'MISCELLANEOUS_NOTICE'><typeOfNoticeToMariner>miscellaneous</typeOfNoticeToMariner><#break>
    <#case 'PRELIMINARY_NOTICE'><typeOfNoticeToMariner>preliminary</typeOfNoticeToMariner><#break>
    <#case 'TEMPORARY_NOTICE'><typeOfNoticeToMariner>temporary</typeOfNoticeToMariner><#break>
    <#case 'PERMANENT_NOTICE'><typeOfNoticeToMariner>permanent</typeOfNoticeToMariner><#break>
    </#switch>
</#macro>

<#macro generalCategory msg>
  <#if msg.categories?has_content >
      <#list msg.categories as cat>
          <@traverseCategories cat=cat/>
      </#list>
  </#if>
</#macro>

<#macro traverseCategories cat>
    <#switch cat.id>
        <#case -1034><generalCategory>aids to navigation</generalCategory><#break>
        <#case -19><generalCategory>dangerous wreck</generalCategory><#break>
        <#case -61><generalCategory>drifting hazard</generalCategory><#break>
        <#case -741><generalCategory>underwater operation</generalCategory><#break>
        <#-- Etc... Map MSI-NM categories to S-124 generalCategory -->
    </#switch>
    <#if cat.parent??>
        <@traverseCategories cat=cat.parent/>
    </#if>
</#macro>

<#macro noticeReference ref>
  <noticeReferences>
      <#switch ref.type>
          <#case 'REFERENCE'><referenceType>source reference</referenceType><#break>
          <#case 'REPETITION'><referenceType>repetition</referenceType><#break>
          <#case 'CANCELLATION'><referenceType>cancellation</referenceType><#break>
          <#case 'UPDATE'><referenceType>update</referenceType><#break>
      </#switch>
      <noticeReference>${ref.seriesIdentifier.fullId}</noticeReference>
  </noticeReferences>
</#macro>

<#macro affectedChart chart>
    <affectedChart>
        <chartAffected>${chart.chartNumber}</chartAffected>
        <#if chart.internationalNumber??>
            <internationalChartAffected>${chart.internationalNumber?c}</internationalChartAffected>
        </#if>
    </affectedChart>
</#macro>

<#macro location loc indx>
    <#assign saveLocale = .locale>
    <#setting locale="en_US">
    <#if loc.type = 'POINT'>
        <s100:pointProperty>
            <s100:point gml:id="loc_${indx}" srsName="http://www.opengis.net/def/crs/EPSG/0/4326">
                <gml:pos><@locationPoints points=loc.points/></gml:pos>
            </s100:point>
        </s100:pointProperty>
    <#elseif loc.type = 'POLYGON'>
        <s100:surfaceProperty>
            <gml:Polygon gml:id="loc_${indx}" srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
                <gml:exterior>
                    <gml:LinearRing>
                        <gml:posList><@locationPoints points=loc.points/></gml:posList>
                    </gml:LinearRing>
                </gml:exterior>
            </gml:Polygon>
        </s100:surfaceProperty>
    <#elseif loc.type = 'POLYLINE'>
        <s100:curveProperty>
            <gml:Curve gml:id="loc_${indx}" srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
                <gml:segments>
                    <gml:LineStringSegment>
                        <gml:posList><@locationPoints points=loc.points/></gml:posList>
                    </gml:LineStringSegment>
                </gml:segments>
            </gml:Curve>
        </s100:curveProperty>
    </#if>
    <#setting locale="${saveLocale}">
</#macro>

<#macro locationPoints points>
    <#list points as point>
        ${point.lat} ${point.lon}
    </#list>
</#macro>
