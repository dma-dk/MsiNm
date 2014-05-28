/**
 * The main MsiNM app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.search', []);
angular.module('msinm.map', []);
angular.module('msinm.user', []);


var app = angular.module('msinm', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'msinm.search', 'msinm.map', 'msinm.user'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/', {
            controller: 'SearchCtrl',
            templateUrl: 'msinm-index.html'
        }).when('/:status', {
            controller: 'SearchCtrl',
            templateUrl: 'msinm-index.html'
        }).otherwise({
            redirectTo: '/'
        });
    }]);

app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
}]);

app.directive('focus', ['$timeout', function($timeout) {
    return function(scope, element, attrs) {
        scope.$watch(attrs.focus, function(newValue) {
            $timeout(function() {
                newValue && element.focus();
            }, 100);
        }, true);
    };
}]);
