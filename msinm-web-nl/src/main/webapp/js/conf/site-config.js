
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = false;
        $rootScope.OAUTH_LOGINS = [ ];

        // Map settings
        $rootScope.DEFAULT_ZOOM_LEVEL = 6;
        $rootScope.DEFAULT_LATITUDE = 52;
        $rootScope.DEFAULT_LONGITUDE = 5;

        // Set current language
        $rootScope.modelLanguages = [ 'nl', 'en' ];
        $rootScope.editorLanguages = $rootScope.modelLanguages;
        $rootScope.siteLanguages = [ 'nl', 'en' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.siteLanguages) > 0)
                ? $window.localStorage.lang
                : $rootScope.siteLanguages[0];
        $window.localStorage.lang = $rootScope.language;
        $translate.use($rootScope.language);

    }]);
