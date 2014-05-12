/**
 * The main MsiNM app module
 *
 * @type {angular.Module}
 */

angular.module('msinm.map', [ ])

var app = angular.module('msinm', ['ngRoute', 'ui.bootstrap', 'msinm.map'])
    .config(function ($routeProvider) {
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
    });
