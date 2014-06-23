
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = true;

        // Set current language
        $rootScope.languages = [ 'en', 'da' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.languages))
                ? $window.localStorage.lang
                : $rootScope.languages[0];
        $translate.use($rootScope.language);

    }]);
