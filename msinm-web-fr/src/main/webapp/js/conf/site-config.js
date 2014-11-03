
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = false;
        $rootScope.OAUTH_LOGINS = [ ];

        // Map settings
        $rootScope.DEFAULT_ZOOM_LEVEL = 4;
        $rootScope.DEFAULT_LATITUDE = 47;
        $rootScope.DEFAULT_LONGITUDE = 3;

        // Set current language
        $rootScope.modelLanguages = [ 'fr', 'en' ];
        $rootScope.editorLanguages = $rootScope.modelLanguages;
        $rootScope.siteLanguages = [ 'fr', 'en' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.siteLanguages) > 0)
                ? $window.localStorage.lang
                : $rootScope.siteLanguages[0];
        $window.localStorage.lang = $rootScope.language;
        $translate.use($rootScope.language);

    }]);