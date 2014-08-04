<#import "mail-template.ftl" as mail/>
<@mail.page title="MSI-NM Report">

<p>MSI-NM Report from ${report.user.email}</p>

<p>
${report.description}
</p>

</@mail.page>
