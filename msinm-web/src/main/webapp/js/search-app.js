/**
 * The main MsiNM search app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.common', []);
angular.module('msinm.search', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'msinm.search', 'msinm.map', 'msinm.user'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/grid', {
            templateUrl: 'partials/search/search-result-grid.html'
        }).when('/map', {
            templateUrl: 'partials/search/search-result-map.html'
        }).when('/table', {
            templateUrl: 'partials/search/search-result-table.html'
        }).otherwise({
            redirectTo: '/grid'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);
