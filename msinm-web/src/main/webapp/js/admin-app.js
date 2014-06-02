/**
 * The main MsiNM admin app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.common', []);
angular.module('msinm.user', []);


var app = angular.module('msinm.admin', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'msinm.user', 'msinm.common' ])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/admin/overview', {
            templateUrl: 'partials/admin/overview.html'
        }).when('/admin/users', {
            templateUrl: 'partials/admin/users.html'
        }).otherwise({
            redirectTo: '/admin/overview'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

