<#import "mail-template.ftl" as mail/>
<@mail.page title="Reset Password">

<p>${text("dear.user", name)}</p>

${text("reset.password.desc")}

<p>
    <a href="/index.html#/resetPassword/${email}/${token}">${baseUri}/index.html#/resetPassword/${email}/${token}</a></li>
</p>

</@mail.page>
