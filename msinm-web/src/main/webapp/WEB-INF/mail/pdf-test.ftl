<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Insert title here</title>

    <style type="text/css" media="all">

        @page {
            size: a4 portrait;
            margin: 1.5cm 1.5cm;
            padding:0;
        }

        body{
            font-size:12px;
            font-family: Helvetica;
            margin: 0;
            padding:0;
        }

        .page-break  {
            clear: left;
            display:block;
            page-break-after:always;
            margin: 0;
            padding: 0;
            height: 0;
        }

        .table-image {
            vertical-align: top;
            padding: 10px;
            border-top: 1px solid lightgray;
        }

        .table-item {
            vertical-align: top;
            width: 100%;
            padding: 10px;
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

    </style>

</head>
<body>

<h1>Messages</h1>
<table>
    <#list messages as msg>
    <tr>
        <td class="table-image">
            <img src="/map-image/${msg.id}.png" width="120" height="120"/>
        </td>
        <td class="table-item">

            <!-- Title line -->
            <#if msg.originalInformation>
                <div>*</div>
            </#if>
            <div>
                <strong>${msg.seriesIdentifier.number}</strong>.
                ${msg.area.descs[0].name} -
                ${msg.descs[0].title}
            </div>

            <table>

                <!-- Reference lines -->
                <#list msg.references as ref>
                <tr>
                    <td class="field-name">
                        <#if ref.type == 'REFERENCE'>
                            EfS reference
                        <#else>
                            Former EfS.
                        </#if>
                    </td>
                    <td class="field-value">
                        ${ref.seriesIdentifier.number} ${'' + ref.seriesIdentifier.year}

                        <#if ref.type == 'REPETITION'>
                            (repitition)
                        <#elseif ref.type == 'REPETITION'>
                            (cancelled)
                        <#elseif ref.type == 'UPDATE'>
                            (updated)
                        </#if>
                    </td>
                </tr>
                </#list>


                <!-- Time line -->
                <#if msg.descs[0].time?has_content>
                <tr>
                    <td class="field-name">Time</td>
                    <td class="field-value">
                        ${msg.descs[0].time}
                    </td>
                </tr>
                </#if>

                <!-- Location line -->
                <#if msg.locations?has_content>
                    <tr>
                        <td class="field-name">Location</td>
                        <td class="field-value">
                            <#list msg.locations as loc>
                                <#if loc.descs?has_content>
                                    <div>${loc.descs[0].description}</div>
                                </#if>
                                <#list loc.points as point>
                                    <div>
                                        ${point.lon} ${point.lon}<#if point.descs?has_content>, ${point.descs[0].description}</#if>
                                    </div>
                                </#list>
                            </#list>
                        </td>
                    </tr>
                </#if>

                <!-- Details line -->
                <#if msg.descs[0].description?has_content>
                    <tr>
                        <td class="field-name">Details</td>
                        <td class="field-value">
                            ${msg.descs[0].description}
                        </td>
                    </tr>
                </#if>

                <!-- Note line -->
                <#if msg.descs[0].note?has_content>
                    <tr>
                        <td class="field-name">Note</td>
                        <td class="field-value">
                        ${msg.descs[0].note}
                        </td>
                    </tr>
                </#if>

                <!-- Charts line -->
                <#if msg.charts?has_content>
                    <tr>
                        <td class="field-name">Charts</td>
                        <td class="field-value">
                            <#list msg.charts as chart>
                                ${chart.chartNumber}
                                <#if chart.internationalNumber?has_content>${chart.internationalNumber}</#if>
                                <#if chart_has_next>, </#if>
                            </#list>
                        </td>
                    </tr>
                </#if>

                <!-- Publication line -->
                <#if msg.descs[0].publication?has_content>
                    <tr>
                        <td class="field-name">Publication</td>
                        <td class="field-value">
                        ${msg.descs[0].publication}
                        </td>
                    </tr>
                </#if>

                <!-- Source line -->
                <#if msg.descs[0].source?has_content>
                    <tr>
                        <td class="field-name" colspan="2">
                        (${msg.descs[0].source})
                        </td>
                    </tr>
                </#if>

            </table>



        </td>
    </tr>
    </#list>
</table>

</body>
</html>
