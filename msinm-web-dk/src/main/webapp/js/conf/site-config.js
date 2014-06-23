
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = true;

        // Set current language
        $rootScope.modelLanguages = [ 'en', 'da' ];
        $rootScope.siteLanguages = [ 'en', 'da' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.languages))
                ? $window.localStorage.lang
                : $rootScope.siteLanguages[0];
        $translate.use($rootScope.language);

    }]);
