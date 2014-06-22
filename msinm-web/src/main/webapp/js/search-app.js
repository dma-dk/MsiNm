/**
 * The main MsiNM search app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.conf', []);
angular.module('msinm.common', []);
angular.module('msinm.search', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'msinm.common', 'msinm.search', 'msinm.conf', 'msinm.map', 'msinm.user'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/search/grid', {
            templateUrl: 'partials/search/search-result-grid.html'
        }).when('/search/map', {
            templateUrl: 'partials/search/search-result-map.html'
        }).when('/search/table', {
            templateUrl: 'partials/search/search-result-table.html'
        }).otherwise({
            redirectTo: '/search/grid'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

