/**
 * The main MsiNM admin app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.conf', []);
angular.module('msinm.common', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm.admin', [
    'ngRoute', 'ngSanitize', 'ngCookies', 'ui.bootstrap', 'angularFileUpload', 'pascalprecht.translate', 'growlNotifications',
    'msinm.conf', 'msinm.user', 'msinm.map', 'msinm.common' ])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/admin/overview', {
            templateUrl: 'partials/admin/overview.html',
            resolve: checkRole('admin')
        }).when('/admin/users', {
            templateUrl: 'partials/admin/users.html',
            resolve: checkRole('admin')
        }).when('/admin/charts', {
            templateUrl: 'partials/admin/charts.html',
            resolve: checkRole('admin')
        }).when('/admin/areas', {
            templateUrl: 'partials/admin/areas.html',
            resolve: checkRole('admin')
        }).when('/admin/categories', {
            templateUrl: 'partials/admin/categories.html',
            resolve: checkRole('admin')
        }).when('/admin/publish', {
            templateUrl: 'partials/admin/publish.html',
            resolve: checkRole('admin')
        }).when('/admin/legacy', {
            templateUrl: 'partials/admin/legacy.html',
            resolve: checkRole('sysadmin')
        }).when('/admin/operations', {
            templateUrl: 'partials/admin/operations.html',
            resolve: checkRole('sysadmin')
        }).when('/admin/settings', {
            templateUrl: 'partials/admin/settings.html',
            resolve: checkRole('sysadmin')
        }).otherwise({
            redirectTo: '/admin/overview'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

