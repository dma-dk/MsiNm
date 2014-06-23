
/**
 * Translations.
 * Danish added
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English',
            'LANG_DA' : 'Danish',

            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register'
        });

        $translateProvider.translations('da', {
            'LANG_EN' : 'Engelsk',
            'LANG_DA' : 'Dansk',

            'MENU_HOME': 'Forside',
            'MENU_SEARCH': 'Søg',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Log ind',
            'MENU_LOGOUT': 'Log ud',
            'MENU_REGISTER': 'Registrering'
        });

        $translateProvider.preferredLanguage('da');

    }]);

