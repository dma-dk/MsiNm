
<#include "message-support.ftl"/>

<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${text("details.title")}</title>

    <@messageStyles />
</head>
<body>



<h1>${text("details.title")}</h1>
<table class="message-table">
    <tr>
        <td class="table-image">
            <img src="/message-map-image/${msg.id?c}.png" width="120" height="120"/>
        </td>
        <td class="table-item">

            <@renderMessage msg=msg />

        </td>
    </tr>
</table>

</body>
</html>
