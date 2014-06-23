
/**
 * Translations.
 * Specific site implementations should add their own translations
 */
angular.module('msinm.conf')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English',

            'MENU_HOME': 'Home',
            'MENU_SEARCH': 'Search',
            'MENU_ADMIN': 'Admin',
            'MENU_LOGIN': 'Login',
            'MENU_LOGOUT': 'Logout',
            'MENU_REGISTER': 'Register'
        });

        $translateProvider.preferredLanguage('en');

    }]);

