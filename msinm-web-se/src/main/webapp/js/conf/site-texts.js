
/**
 * Translations.
 * Danish added
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English',
            'LANG_SV' : 'Svenska',

            'MENU_BRAND': 'MSI-NM',
            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_REPORT': 'Report',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register',

            'FOOTER_COPYRIGHT': '&copy; 2014 Sjöfartsverket',
            'FOOTER_DISCLAIMER': 'Disclaimer',
            'FOOTER_COOKIES': 'Cookies',

            'FRONT_PAGE_TEASER': '<h1>Welcome to MSI-NM</h1><p>MSI-NM is an effort by the Danish Maritime Authority to combine MSI and NM P&T.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>Active Warnings</h2><p>For a detailed list of the active MSI-NM warnings, please go to the <a href="/search.html">Search</a> section.</p>',
            'FRONT_PAGE_FIRING_TITLE': '<h2>Firing Exercises</h2>',
            'FRONT_PAGE_FIRING_DESC': '<p>Please find a summary of the firing exercises below</p>',
            'FRONT_PAGE_FIRING_TODAY': '<h4>Firing Exercises Today</h4>',
            'FRONT_PAGE_FIRING_TOMORROW': '<h4>Firing Exercises Tomorrow</h4>',
            'FRONT_PAGE_MISC': '<h2>Report Observations</h2><p>If you observe an incident relevant to the maritime community, '
                + 'please file a report in the <a href="/report.html">Report</a> section</p>'
                + '<h2>Other sources</h2><p>Please find the official MSI and NM\'s for the Swedish Maritime Area at:</p>'
                + '<ul><li><a href="http://www.sjofartsverket.se" target="_blank">Sjöfartsverket</a></ul>'

        });

        $translateProvider.translations('sv', {
            'LANG_EN' : 'English',
            'LANG_SV' : 'Svenska',

            'MENU_BRAND': 'MSI-NM',
            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_REPORT': 'Report',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register',

            'FOOTER_COPYRIGHT': '&copy; 2014 Sjöfartsverket',
            'FOOTER_DISCLAIMER': 'Disclaimer',
            'FOOTER_COOKIES': 'Cookies',

            'FRONT_PAGE_TEASER': '<h1>Welcome to MSI-NM</h1><p>MSI-NM is an effort by the Danish Maritime Authority to combine MSI and NM P&T.</p>',
            'FRONT_PAGE_ACTIVE_WARNINGS': '<h2>Active Warnings</h2><p>For a detailed list of the active MSI-NM warnings, please go to the <a href="/search.html">Search</a> section.</p>',
            'FRONT_PAGE_FIRING_TITLE': '<h2>Firing Exercises</h2>',
            'FRONT_PAGE_FIRING_DESC': '<p>Please find a summary of the firing exercises below</p>',
            'FRONT_PAGE_FIRING_TODAY': '<h4>Firing Exercises Today</h4>',
            'FRONT_PAGE_FIRING_TOMORROW': '<h4>Firing Exercises Tomorrow</h4>',
            'FRONT_PAGE_MISC': '<h2>Report Observations</h2><p>If you observe an incident relevant to the maritime community, '
                + 'please file a report in the <a href="/report.html">Report</a> section</p>'
                + '<h2>Other sources</h2><p>Please find the official MSI and NM\'s for the Swedish Maritime Area at:</p>'
                + '<ul><li><a href="http://www.sjofartsverket.se" target="_blank">Sjöfartsverket</a></ul>'
        });

        $translateProvider.preferredLanguage('sv');

    }]);

