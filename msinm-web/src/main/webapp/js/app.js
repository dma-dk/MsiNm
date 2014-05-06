/**
 * The main MsiNM app module
 *
 * @type {angular.Module}
 */
var app = angular.module('msinm', ['ngRoute', 'ui.bootstrap'])
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
