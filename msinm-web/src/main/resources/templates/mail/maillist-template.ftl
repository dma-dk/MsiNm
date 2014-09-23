<#macro page title>
<html>
    <head>
        <title>${title?html}</title>
        <link rel="stylesheet" href="/css/maillist.css">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    </head>
    <body>

    <div class="mail">
        <div class="navbar">
            <img src="/img/logo.png" height="40">
        </div>

        <div class="mail-content">
            <h1>${title?html}</h1>

            <#nested/>

            <p>
               ${text("greetings.best.regards")} <br/>
                <strong>${text("greetings.sender.name")}</strong><br/>
                <a href="http://${text("greetings.sender.url")}" title="${text("greetings.sender.name")}">${text("greetings.sender.url")}</a>
            </p>
        </div>

    </div>


    </body>
</html>
</#macro>
