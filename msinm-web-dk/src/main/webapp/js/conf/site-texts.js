
/**
 * Translations.
 * Danish added
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register'
        });

        $translateProvider.translations('da', {
            'MENU_HOME': 'Forside',
            'MENU_SEARCH': 'Søg',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Log ind',
            'MENU_LOGOUT': 'Log ud',
            'MENU_REGISTER': 'Registrering'
        });

        $translateProvider.preferredLanguage('da');

    }]);

