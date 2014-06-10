<#import "mail-template.ftl" as mail/>
<@mail.page title="Reset Password">

<p>Dear ${name}</p>
<p>In order to reset your password at MSI-NM, please open the link below at enter the new password.</p>
<p>
    <a href="/index.html#/resetPassword/${email}/${token}">${baseUri}/index.html#/resetPassword/${email}/${token}</a></li>
</p>

</@mail.page>
