<#import "mail-template.ftl" as mail/>
<@mail.page title="Welcome to MSI-NM">

<p>Dear ${name}</p>
<p>You have been registered as a user for the MSI-NM website.</p>
<p>Your user ID is <strong>${email}</strong>.</p>
<p>In order to complete registration and choose your own password, please use the link below:</p>
<p>
    <a href="/index.html#/resetPassword/${email}/${token}">${baseUri}/index.html#/resetPassword/${email}/${token}</a></li>
</p>

</@mail.page>
