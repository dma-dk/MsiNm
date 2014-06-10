<#macro page title>
<html>
    <head>
        <title>${title?html}</title>
        <link rel="stylesheet" href="/css/mail.css">
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
                Best regards, <br/>
                <strong>Danish Maritime Authority</strong><br/>
                <a href="http://www.dma.dk" title="Danish Maritime Authority">www.dma.dk</a>
            </p>
        </div>

    </div>


    </body>
</html>
</#macro>
