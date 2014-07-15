<#import "mail-template.ftl" as mail/>
<@mail.page title="Welcome to MSI-NM">

<p>${text("dear.user", name)}</p>

${text("user.registration.desc", email)}

<p>
    <a href="/index.html#/resetPassword/${email}/${token}">${baseUri}/index.html#/resetPassword/${email}/${token}</a></li>
</p>

</@mail.page>
