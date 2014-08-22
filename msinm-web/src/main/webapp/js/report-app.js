/**
 * The main MsiNM Report app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.conf', []);
angular.module('msinm.common', []);
angular.module('msinm.user', []);
angular.module('msinm.map', []);


var app = angular.module('msinm.report', [
    'ngRoute', 'ngSanitize', 'ngCookies', 'ui.bootstrap', 'angularFileUpload', 'pascalprecht.translate', 'growlNotifications',
    'msinm.conf', 'msinm.user', 'msinm.common', 'msinm.map', 'msinm.report'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/report/intro', {
            templateUrl: 'partials/report/report-intro.html',
        }).when('/report/submit-report', {
            templateUrl: 'partials/report/submit-report.html',
            resolve: checkRole('user')
        }).otherwise({
            redirectTo: '/report/intro'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

