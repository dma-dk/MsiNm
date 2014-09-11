
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
        $rootScope.DEFAULT_LATITUDE = 61;
        $rootScope.DEFAULT_LONGITUDE = 18;

        // Set current language
        $rootScope.modelLanguages = [ 'sv', 'en' ];
        $rootScope.editorLanguages = [ 'sv', 'en', 'fi' ];
        $rootScope.siteLanguages = [ 'sv', 'en' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.siteLanguages) > 0)
                ? $window.localStorage.lang
                : $rootScope.siteLanguages[0];
        $window.localStorage.lang = $rootScope.language;
        $translate.use($rootScope.language);

    }]);
