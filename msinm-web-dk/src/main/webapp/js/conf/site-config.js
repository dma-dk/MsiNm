
/**
 * Common settings.
 * The settings are either defined as constants or set on the root scope
 */
angular.module('msinm.conf')

    .run(['$rootScope', '$translate', '$window', function ($rootScope, $translate, $window) {

        $rootScope.LEGACY_ADMIN_PAGE = true;
        $rootScope.OAUTH_LOGINS = [ { url: '/oidc-login', name: 'Maritime ID', icon: 'fa fa-anchor' }, { url: '/oauth/login/google', name: 'Google', icon: 'fa fa-google' } ];

        // Map settings
        $rootScope.DEFAULT_ZOOM_LEVEL = 6;
        $rootScope.DEFAULT_LATITUDE = 56;
        $rootScope.DEFAULT_LONGITUDE = 11;

        // Set current language
        $rootScope.modelLanguages = [ 'da', 'en' ];
        $rootScope.editorLanguages = [ 'da', 'en', 'gl' ];
        $rootScope.siteLanguages = [ 'da', 'en' ];
        $rootScope.language =
            ($window.localStorage.lang && $.inArray($window.localStorage.lang, $rootScope.siteLanguages) > 0)
                ? $window.localStorage.lang
                : $rootScope.siteLanguages[0];
        $window.localStorage.lang = $rootScope.language;
        $translate.use($rootScope.language);

    }]);
