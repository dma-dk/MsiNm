/**
 * The main MsiNM admin app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.common', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm.admin', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'ui.tree', 'angularFileUpload', 'msinm.user', 'msinm.map', 'msinm.common' ])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/admin/overview', {
            templateUrl: 'partials/admin/overview.html',
            resolve: checkRole('admin')
        }).when('/admin/users', {
            templateUrl: 'partials/admin/users.html',
            resolve: checkRole('admin')
        }).when('/admin/legacy', {
            templateUrl: 'partials/admin/legacy.html',
            resolve: checkRole('admin')
        }).when('/admin/operations', {
            templateUrl: 'partials/admin/operations.html',
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
        }).otherwise({
            redirectTo: '/admin/overview'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

