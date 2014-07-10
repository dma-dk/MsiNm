
/**
 * Translations.
 * Danish added
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English',
            'LANG_DA' : 'Danish',

            'MENU_BRAND': 'MSI-NM',
            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register',

            'FOOTER_COPYRIGHT': '&copy; 2014 Danish Maritime Authority',
            'FOOTER_DISCLAIMER': 'Disclaimer',
            'FOOTER_COOKIES': 'Cookies',

            'FRONT_PAGE_TEASER': '<h1>Welcome to MSI-NM</h1><p>MSI-NM is an effort by the Danish Maritime Authority to combine MSI and MN P&T.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>Active Warnings</h2><p>For a detailed list of the active MSI-NM warnings, please go to the <a href="/search.html">Search</a> section.</p>',
            'FRONT_PAGE_INTEGRATION_1': '<h2>Integration</h2><p>MSI and NM warnings can be accessed via a public JSON-API, as exemplified below:</p>',
            'FRONT_PAGE_INTEGRATION_2': '<p>Furthermore, the API contains functionality that may be used by registered users.</p>'
                + '<h3>Open Source</h3><p>The MSI-NM project is Open Source. You can download the entire project and customize it for your own needs.</p>'
                + '<ul><li><a href="https://github.com/dma-dk/MsiNm" target="_blank">MsiNm at Github</a></li></ul>',
            'FRONT_PAGE_MISC': '<h2>Other sources</h2><p>Please find the official MSI and NM\'s for the Danish Maritime Area at:</p>'
                + '<ul><li><a href="http://www.soefartsstyrelsen.dk/AdvarslerEfterretninger/Advarsler/Sider/default.aspx" target="_blank">Active MSI - DK</a></li>'
                + '<li><a href="http://www.soefartsstyrelsen.dk/AdvarslerEfterretninger/EfterretningerForSoefarende/Sider/Default.aspx" target="_blank">Current and Historical NM - DK</a></li></ul>'

        });

        $translateProvider.translations('da', {
            'LANG_EN' : 'Engelsk',
            'LANG_DA' : 'Dansk',

            'MENU_BRAND': 'MSI-NM',
            'MENU_HOME': 'Forside',
            'MENU_SEARCH': 'Søg',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Log ind',
            'MENU_LOGOUT': 'Log ud',
            'MENU_REGISTER': 'Registrering',

            'FOOTER_COPYRIGHT': '&copy; 2014 Søfartsstyrelsen',
            'FOOTER_DISCLAIMER': 'Disclaimer',
            'FOOTER_COOKIES': 'Cookies',


            'FRONT_PAGE_TEASER': '<h1>Velkommen til MSI-NM</h1><p>MSI-NM er et projekt fra den Danske Søfartsstyrelse hvor man kombinerer MSI og MN P&T.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>Aktive advarsler</h2><p>For en detaljeret liste over MSI-NM advarsler og efterretninger, benyt <a href="/search.html">Søgningen</a>.</p>',
            'FRONT_PAGE_INTEGRATION_1': '<h2>Integration</h2><p>MSI og NM advarsler kan blive tilgået via et JSON-API, som eksemplificeret nedenfor:</p>',
            'FRONT_PAGE_INTEGRATION_2': '<p>Desuden udbyder API\'et funktionalitet der kan benyttes af registrerede parter.</p>'
                + '<h3>Open Source</h3><p>MSI-NM projetet er Open Source. Projektet kan downloaded og tilrettes efter egne formål.</p>'
                + '<ul><li><a href="https://github.com/dma-dk/MsiNm" target="_blank">MsiNm på Github</a></li></ul>',
            'FRONT_PAGE_MISC': '<h2>Øvrige kilder</h2><p>Den officielle liste af MSI og NM\'er for det Danske maritime område findes:</p>'
                + '<ul><li><a href="http://www.soefartsstyrelsen.dk/AdvarslerEfterretninger/Advarsler/Sider/default.aspx" target="_blank">Aktive MSI - DK</a></li>'
                + '<li><a href="http://www.soefartsstyrelsen.dk/AdvarslerEfterretninger/EfterretningerForSoefarende/Sider/Default.aspx" target="_blank">Nuværende og historiske NM - DK</a></li></ul>'
        });

        $translateProvider.preferredLanguage('da');

    }]);

