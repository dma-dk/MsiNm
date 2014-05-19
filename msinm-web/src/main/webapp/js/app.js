/**
 * The main MsiNM app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.map', [ ])

var app = angular.module('msinm', ['ngRoute', 'ngCookies', 'ui.bootstrap', 'msinm.map', 'msinm.user'])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/', {
            controller: 'MsiNmCtrl',
            templateUrl: 'msinm-index.html'
        }).when('/:status', {
            controller: 'MsiNmCtrl',
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
