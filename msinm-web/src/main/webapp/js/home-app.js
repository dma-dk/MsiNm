/**
 * The main MsiNM front page app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.conf', []);
angular.module('msinm.common', []);
angular.module('msinm.search', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm.home', [
    'ngRoute', 'ngSanitize', 'ngCookies', 'ui.bootstrap', 'pascalprecht.translate', 'growlNotifications',
    'msinm.conf', 'msinm.user', 'msinm.common', 'msinm.search', 'msinm.map'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/', {
            templateUrl: 'partials/home/home.html'
        }).when('/resetPassword/:email/:token', {
            templateUrl: 'partials/home/home.html'
        }).when('/auth/:authToken', {
            templateUrl: 'partials/home/home.html'
        }).when('/message/:messageId', {
            templateUrl: 'partials/home/home.html'
        }).otherwise({
            redirectTo: '/'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

