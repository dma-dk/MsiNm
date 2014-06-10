/**
 * The main MsiNM front page app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.common', []);
angular.module('msinm.search', []);
angular.module('msinm.user', []);


var app = angular.module('msinm.home', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'msinm.user', 'msinm.common', 'msinm.search'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/', {
            templateUrl: 'partials/home/home.html'
        }).when('/resetPassword/:email/:token', {
            templateUrl: 'partials/home/home.html'
        }).otherwise({
            redirectTo: '/'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

