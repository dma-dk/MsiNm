
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = false;
        $rootScope.OAUTH_LOGINS = [ ];

        // Map settings
        $rootScope.DEFAULT_ZOOM_LEVEL = 5;
        $rootScope.DEFAULT_LATITUDE = -17;
        $rootScope.DEFAULT_LONGITUDE = 146;

        // Set current language
        $rootScope.modelLanguages = [ 'en' ];
        $rootScope.editorLanguages = $rootScope.modelLanguages;
        $rootScope.siteLanguages = [ 'en' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.siteLanguages) > 0)
                ? $window.localStorage.lang
                : $rootScope.siteLanguages[0];
        $window.localStorage.lang = $rootScope.language;
        $translate.use($rootScope.language);

    }]);
