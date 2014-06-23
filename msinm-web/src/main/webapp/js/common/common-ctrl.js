/**
 * The home controller
 */
angular.module('msinm.common')
    .controller('FooterCtrl', ['$scope', '$modal',
        function ($scope, $modal) {
        'use strict';

        $scope.cookiesDlg = function () {
            $modal.open({
                templateUrl: '../../partials/common/cookies.html',
                size: 'lg'
            });
        };

        $scope.disclaimerDlg = function () {
            $modal.open({
                templateUrl: '../../partials/common/disclaimer.html',
                size: 'lg'
            });
        }
    }])

    /**
     * Language Controller
     */
    .controller('LangCtrl', ['$rootScope', '$scope', '$window', '$translate',
        function ($rootScope, $scope, $window, $translate) {
        'use strict';

        $scope.changeLanguage = function(lang) {
            $translate.use(lang);
            $rootScope.LANGUAGE = lang;
            $window.localStorage.lang = lang;
        }

    }]);
