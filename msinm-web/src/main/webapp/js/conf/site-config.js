
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = false;

        $rootScope.LANGUAGES = [ 'en' ];
        $rootScope.LANGUAGE =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.LANGUAGES))
            ? $window.localStorage.lang
            : $rootScope.LANGUAGES[0];
        $translate.use($rootScope.LANGUAGE);

    }]);
