
/**
 * Translations.
 * Specific site implementations should add their own translations
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English',

            'MENU_BRAND': 'MSI-NM UK',
            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register',

            'FOOTER_COPYRIGHT': '&copy; 2014 UK Hydrographic Office',
            'FOOTER_DISCLAIMER': 'Disclaimer',
            'FOOTER_COOKIES': 'Cookies',

            'FRONT_PAGE_TEASER': '<h1>Welcome to MSI-NM UK</h1><p>MSI-NM is an effort by the UK Hydrographic Office to combine MSI and MN P&T.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>Active Warnings</h2><p>For a detailed list of the active MSI-NM warnings, please go to the <a href="/search.html">Search</a> section.</p>',
            'FRONT_PAGE_INTEGRATION_1': '<h2>Integration</h2><p>MSI and NM warnings can be accessed via a public JSON-API, as exemplified below:</p>',
            'FRONT_PAGE_INTEGRATION_2': '<p>Furthermore, the API contains functionality that may be used by registered users.</p>'
                    + '<h3>Open Source</h3><p>The MSI-NM project is Open Source. You can download the entire project and customize it for your own needs.</p>'
                    + '<ul><li><a href="https://github.com/dma-dk/MsiNm" target="_blank">MsiNm at Github</a></li></ul>',
            'FRONT_PAGE_MISC': '<h2>Other sources</h2><p>Please find the official MSI and NM\'s for the UK Area at:</p>'
                    + '<ul><li><a href="http://www.ukho.gov.uk/ProductsandServices/MartimeSafety/Pages/Home.aspx" target="_blank">MSI - UK</a></li>'
                    + '<li><a href="http://www.ukho.gov.uk/nmwebsearch/" target="_blank">Searchable NM\'s</a></li></ul>'

        });

        $translateProvider.preferredLanguage('en');

    }]);
