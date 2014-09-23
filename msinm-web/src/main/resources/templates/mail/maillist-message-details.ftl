<#import "maillist-template.ftl" as mail/>
<#include "../pdf/message-support.ftl"/>

<@mail.page title="${mailList.name}">

    <p>${text("dear.user", name)}</p>

    <p>${text("message.details.intro", mailList.name)}</p>

    <table class="message-table">
        <tr>
            <td class="table-image">
                <a href="${baseUri}/index.html#/message/${message.id?c}" target="_blank">
                    <img src="/message-map-image/${message.id?c}.png" width="120" height="120"/>
                </a>
            </td>
            <td class="table-item">

                <@renderMessage msg=message />

            </td>
        </tr>
    </table>

</@mail.page>
