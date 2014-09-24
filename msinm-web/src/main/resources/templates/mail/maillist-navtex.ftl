<#import "maillist-template.ftl" as mail/>

<@mail.page title="${mailList.name}">

<p>${text("dear.user", name)}</p>

<p>${text("navtex.intro", mailList.name)}</p>

<pre style="color: #aa0000">
${navtexData.message}
</pre>

</@mail.page>
